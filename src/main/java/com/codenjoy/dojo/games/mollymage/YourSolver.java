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
import java.util.Deque;

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
        SearchField searchField = new SearchField();
        searchField.searchFor(Element.TREASURE_BOX, me);
        if (searchField.isFound()) {
            return Command.MOVE.apply(searchField.backTrace());
        }
        return Command.DROP_POTION;
    }

    public class SearchField {
        Point found;
        int totalSteps;
        int[][] distances;
        Deque<Point> searchQueue;

        public SearchField() {
            this.distances = new int[boardSize][boardSize];
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    distances[i][j] = 999;
                }
            }
            searchQueue = new ArrayDeque<>();
        }

        void searchFor(Element element, Point coords) {
            Element here = board.getAt(coords.getX(), coords.getY());
            if (here.equals(Element.HERO)) {
                distances[coords.getX()][coords.getY()] = 0;
            }
            var currentDistance = distances[coords.getX()][coords.getY()];
            if (here.equals(element)) {
                found = coords;
                totalSteps = currentDistance;
            } else {
                for (int direction = 0; direction < 4; direction++) {
                    var next = coords.copy();
                    next.move(Direction.valueOf(direction));
                    Element nextElem = board.getAt(next);
                    if (distances[next.getX()][next.getY()] > currentDistance + 1 &&
                            (nextElem.equals(Element.NONE) || nextElem.equals(element))) {
                        distances[next.getX()][next.getY()] = currentDistance + 1;
                        searchQueue.add(next);
                    }
                }
            }
            if (!isFound() && !searchQueue.isEmpty()) {
                var nextPoint = searchQueue.pollFirst();
                searchFor(element, nextPoint);
            }
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