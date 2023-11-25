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
 * Pay attention to {@link YourSolverTest} - there is
 * a test framework for you.
 */
public class YourSolver implements Solver<Board> {

    private Dice dice;
    private Board board;
    private Point me;

    private int boardSize;

    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    @Override
    public String get(Board board) {
        this.board = board;
        if (board.isGameOver()) return "";
        boardSize = board.size();
        me = board.getHero();
        int[] dirScores = new int[6];
        Point[] nearPoints = IntStream.range(0,6).mapToObj(n -> nearMe(Direction.valueOf(n))).toArray(Point[]::new);

        SearchField boxSearch = new SearchField(Element.TREASURE_BOX).searchFrom(me);
        SearchField ghostSearch = new SearchField(Element.GHOST).searchFrom(me);
        SearchField potionSearch = new SearchField(Element.POTION_IMMUNE, Element.POTION_COUNT_INCREASE, Element.POTION_BLAST_RADIUS_INCREASE, Element.POTION_REMOTE_CONTROL, Element.POISON_THROWER, Element.POTION_EXPLODER).searchFrom(me);

        for (int i = 0; i < 6; i++) {
            Point pt = nearPoints[i];
            Direction dir = Direction.valueOf(i);

            if (board.isFutureBlastAt(pt)) dirScores[i] -=100;
            if (ghostSearch.isFound() && ghostSearch.totalSteps<4) {
                dirScores[ghostSearch.backTrace().ordinal()] -= 50 / ghostSearch.totalSteps;
            }
            if (boxSearch.isFound()) {
                dirScores[boxSearch.backTrace().ordinal()] += 20;
            }
            if (potionSearch.isFound()) {
                dirScores[potionSearch.backTrace().ordinal()] += 30 / potionSearch.totalSteps;
            }
        }
        int bestIndex = 0;
        int bestValue = -999;
        for (int i = 0; i < 6; i++) {
            if (dirScores[i]>bestValue) {
                bestValue = dirScores[i];
                bestIndex = i;
            }
        }
        if (bestIndex == 5) return ghostSearch.isFound() && ghostSearch.totalSteps < 3? Command.DROP_POTION : "";

        if ((boxSearch.isFound() && boxSearch.totalSteps == 1)||(ghostSearch.isFound() &&ghostSearch.totalSteps < 3)) {
            return Command.DROP_POTION_THEN_MOVE.apply(Direction.valueOf(bestIndex));
        }

        return Command.MOVE.apply(Direction.valueOf(bestIndex));
    }

    private Point nearMe(Direction direction) {
        Point copy = me.copy();
        copy.move(direction);
        return copy;
    }

    public Direction findNearMe(Element element) {
        for (Direction dir : Direction.values()) {
            Point near = me.copy();
            near.move(dir);
            if (board.getAt(near) == element && !board.isFutureBlastAt(near)) return dir;
        }
        return null;
    }

    public String evadeBlast() {
        List<Point> futureBlasts = board.getFutureBlasts();
        if (!futureBlasts.contains(me)) return null;
        for (Direction dir : Direction.values()) {
            Point near = me.copy();
            near.move(dir);
            if (!futureBlasts.contains(near)) return Command.MOVE.apply(dir);
        }
        return null;
    }

    public boolean checkIfSafe(Direction dir) {
        Point copy = me.copy();
        copy.move(dir);
        return !board.isFutureBlastAt(copy);
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
                            (nextElem.equals(Element.NONE) || elementsToSearch.contains(nextElem))) {
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