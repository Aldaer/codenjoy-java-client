package com.codenjoy.dojo.games.mollymage;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2012 - 2022 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.PointImpl;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Author: your name
 * <p>
 * This is your AI algorithm for the game.
 * Implement it at your own discretion.
 * Pay attention to YourSolverTest - there is
 * a test framework for you.
 */
public class YourSolver implements Solver<Board> {

    private final Dice dice;
    private Board board;
    private Point me;

    private int boardSize;

    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    private Point gotHereFrom = new PointImpl();

    @Override
    public String get(Board board) {
        this.board = board;
        if (board.isGameOver()) return "";
        boardSize = board.size();
        me = board.getHero();
        int[] dirScores = new int[6];
        Point[] nearPoints = IntStream.range(0, 6).mapToObj(n -> nearMe(Direction.valueOf(n))).toArray(Point[]::new);

        SearchField boxSearch = new SearchField(Element.TREASURE_BOX).searchFrom(me);
        SearchField ghostSearch = new SearchField(Element.GHOST).searchFrom(me);
        SearchField potionSearch = new SearchField(Element.POTION_IMMUNE, Element.POTION_COUNT_INCREASE, Element.POTION_BLAST_RADIUS_INCREASE, Element.POTION_REMOTE_CONTROL, Element.POISON_THROWER, Element.POTION_EXPLODER).searchFrom(me);

        for (int i = 0; i < 6; i++) {
            Point pt = nearPoints[i];

            if (isNoGo(pt)) dirScores[i] -= 100;
            if (gotHereFrom.equals(pt)) dirScores[i] -= 10;
        }
        if (ghostSearch.isFound() && ghostSearch.totalSteps < 4) {
            dirScores[ghostSearch.backTrace().ordinal()] -= 50 / ghostSearch.totalSteps;
        }
        if (boxSearch.isFound() && boxSearch.totalSteps < 10) {
            dirScores[boxSearch.backTrace().ordinal()] += 20;
        }
        if (potionSearch.isFound() && potionSearch.totalSteps < 8) {
            dirScores[potionSearch.backTrace().ordinal()] += 50 / potionSearch.totalSteps;
        }
        System.out.printf("Weights: [%d,%d,%d,%d,%d,%d]", dirScores[0], dirScores[1], dirScores[2], dirScores[3], dirScores[4], dirScores[5]);

        int bestIndex = 0;
        int bestValue = -999;
        for (int i = 0; i < 6; i++) {
            if (dirScores[i] > bestValue) {
                bestValue = dirScores[i];
                bestIndex = i;
            }
        }
        String result;
        if (bestIndex == 5) {
            result = ghostSearch.isFound() && ghostSearch.totalSteps < 3 ? Command.DROP_POTION : "";
        } else if ((boxSearch.isFound() && boxSearch.totalSteps == 1) || (ghostSearch.isFound() && ghostSearch.totalSteps < 3)) {
            result = Command.DROP_POTION_THEN_MOVE.apply(Direction.valueOf(bestIndex));
        } else result = Command.MOVE.apply(Direction.valueOf(bestIndex));
        gotHereFrom = me;
        return result;
    }

    private Point nearMe(Direction direction) {
        Point copy = me.copy();
        copy.move(direction);
        return copy;
    }

    public boolean isNoGo(Point pt) {
        return board.isFutureBlastAt(pt) || board.isWallAt(pt) || board.isBarrierAt(pt) || board.isTreasureBoxAt(pt) || board.isGhostAt(pt) || board.isEnemyHeroAt(pt);
    }

    public class SearchField {
        private final List<Element> elementsToSearch;
        Point found;
        int totalSteps;
        int[][] distances;
        Deque<Point> searchQueue;

        public SearchField(Element... elementsToSearch) {
            this.elementsToSearch = Arrays.asList(elementsToSearch);
            this.distances = new int[boardSize][boardSize];
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    distances[i][j] = 999;
                }
            }
            searchQueue = new ArrayDeque<>();
        }

        SearchField searchFrom(Point coords) {
            Element here = board.getAt(coords.getX(), coords.getY());
            if (here.equals(Element.HERO)) {
                distances[coords.getX()][coords.getY()] = 0;
            }
            var currentDistance = distances[coords.getX()][coords.getY()];
            if (elementsToSearch.contains(here)) {
                found = coords;
                totalSteps = currentDistance;
            } else {
                for (int direction = 0; direction < 4; direction++) {
                    var next = coords.copy();
                    next.move(Direction.valueOf(direction));
                    Element nextElem = board.getAt(next);
                    if (distances[next.getX()][next.getY()] > currentDistance + 1 &&
                            (nextElem.equals(Element.NONE) || elementsToSearch.contains(nextElem)))
                    {
                        distances[next.getX()][next.getY()] = currentDistance + 1;
                        searchQueue.add(next);
                    }
                }
            }
            if (!isFound() && !searchQueue.isEmpty()) {
                var nextPoint = searchQueue.pollFirst();
                searchFrom(nextPoint);
            }
            return this;
        }

        boolean isFound() {
            return found != null;
        }

        Direction backTrace() {
            int currDistance = totalSteps;
            Point currPoint = found;
            Direction lastMove = null;
            while (currDistance > 0) {
                int dir = 0;
                Direction move;
                Point next;
                do {
                    move = Direction.valueOf(dir++);
                    next = currPoint.copy();
                    next.move(move);
                } while (distances[next.getX()][next.getY()] >= currDistance);
                currDistance--;
                currPoint = next;
                lastMove = move;
            }
            return lastMove.inverted();
        }
    }
}