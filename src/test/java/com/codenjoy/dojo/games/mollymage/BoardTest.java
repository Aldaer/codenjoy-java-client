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


import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.dice.RandomDice;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.codenjoy.dojo.services.PointImpl.pt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BoardTest {

    private Board board;

    public static Board board(String boardString) {
        return (Board)new Board().forString(boardString);
    }

    @Before
    public void before() {
        board = board(
           /*8*/"☼☼☼☼☼☼☼☼☼" +
           /*7*/"☼1 ♣   ♠☼" +
           /*6*/"☼#2  &  ☼" +
           /*5*/"☼# 3 ♣ ♠☼" +
           /*4*/"☼☺  4   ☼" +
           /*3*/"☼   ö H☻☼" +
           /*2*/"☼x H ҉҉҉☼" +
           /*1*/"☼& &    ☼" +
           /*0*/"☼☼☼☼☼☼☼☼☼");
               /*012345678*/
    }

    @Test
    public void shouldWorkToString() {
        assertBoard(
                "☼☼☼☼☼☼☼☼☼\n" +
                "☼1 ♣   ♠☼\n" +
                "☼#2  &  ☼\n" +
                "☼# 3 ♣ ♠☼\n" +
                "☼☺  4   ☼\n" +
                "☼   ö H☻☼\n" +
                "☼x H ҉҉҉☼\n" +
                "☼& &    ☼\n" +
                "☼☼☼☼☼☼☼☼☼\n" +
                "\n" +
                "Hero at: [1,4]\n" +
                "Other heroes at: [[3,7], [5,5], [7,5], [7,7]]\n" +
                "Enemy heroes at: [[4,3]]\n" +
                "Ghosts at: [[1,1], [1,2], [3,1], [5,6]]\n" +
                "Treasure boxes at: [[1,5], [1,6], [3,2], [6,3]]\n" +
                "Potions at: [[1,7], [2,6], [3,5], [4,4], [7,3], [7,5], [7,7]]\n" +
                "Blasts: [[5,2], [6,2], [7,2]]\n" +
                "Expected blasts at: [[2,7]]");
    }

    private void assertBoard(String expected) {
        assertEquals(expected, board.toString());
    }

    @Test
    public void testFindDirection() {
        var solver = new YourSolver(new RandomDice());
        solver.get(board);
        YourSolver.SearchField searchField = solver.new SearchField(Element.POTION_TIMER_2);
        searchField.searchFrom(board.getHero());
        assertEquals(Direction.RIGHT, searchField.backTrace());
    }

    @Test
    public void shouldWork_getAt() {
        assertEquals(Element.HERO_POTION, board.getAt(7, 3));
        assertEquals(Element.WALL, board.getAt(0, 8));
        assertEquals(Element.GHOST, board.getAt(5, 6));
    }

    @Test
    public void shouldWork_getAt_point() {
        assertEquals(Element.HERO_POTION, board.getAt(pt(7, 3)));
        assertEquals(Element.WALL, board.getAt(pt(0, 8)));
        assertEquals(Element.GHOST, board.getAt(pt(5, 6)));
    }

    @Test
    public void shouldWork_getNear() {
        assertEquals("[H, ҉,  , ☼]", board.getNear(7, 3).toString());
        assertEquals("[☼, ☼]", board.getNear(0, 8).toString());
        assertEquals("[ , ♣,  ,  ]", board.getNear(5, 6).toString());
    }

    @Test
    public void shouldWork_getNear_point() {
        assertEquals("[H, ҉,  , ☼]", board.getNear(pt(7, 3)).toString());
        assertEquals("[☼, ☼]", board.getNear(pt(0, 8)).toString());
        assertEquals("[ , ♣,  ,  ]", board.getNear(pt(5, 6)).toString());
    }

    @Test
    public void shouldWork_getAt_outOfBoard() {
        assertEquals(Element.WALL, board.getAt(-1, 1));
        assertEquals(Element.WALL, board.getAt(1, -1));
        assertEquals(Element.WALL, board.getAt(100, 1));
        assertEquals(Element.WALL, board.getAt(1, 100));

        assertEquals(Element.WALL, board.getAt(pt(-1, 1)));
        assertEquals(Element.WALL, board.getAt(pt(1, -1)));
        assertEquals(Element.WALL, board.getAt(pt(100, 1)));
        assertEquals(Element.WALL, board.getAt(pt(1, 100)));
    }

    @Test
    public void shouldWork_getOtherHeroes() {
        assertEquals("[[3,7], [5,5], [7,5], [7,7]]",
                board.getOtherHeroes().toString());
    }

    @Test
    public void shouldWork_isOtherHeroAt_point() {
        assertEquals(true, board.isOtherHeroAt(pt(5, 5)));
        assertEquals(false, board.isOtherHeroAt(pt(5, 6)));

        assertEquals(true, board("♥").isOtherHeroAt(pt(0, 0)));
        assertEquals(true, board("♠").isOtherHeroAt(pt(0, 0)));
        assertEquals(true, board("♣").isOtherHeroAt(pt(0, 0)));

        assertEquals(false, board("&").isOtherHeroAt(pt(0, 0)));
    }

    @Test
    public void shouldWork_getEnemyHeroes() {
        assertEquals("[[4,3]]",
                board.getEnemyHeroes().toString());
    }

    @Test
    public void shouldWork_isEnemyHeroAt_point() {
        assertEquals(true, board.isEnemyHeroAt(pt(4, 3)));
        assertEquals(false, board.isEnemyHeroAt(pt(4, 4)));

        assertEquals(true, board("ö").isEnemyHeroAt(pt(0, 0)));
        assertEquals(true, board("Ö").isEnemyHeroAt(pt(0, 0)));
        assertEquals(true, board("ø").isEnemyHeroAt(pt(0, 0)));

        assertEquals(false, board("&").isEnemyHeroAt(pt(0, 0)));
    }

    @Test
    public void shouldWork_getBarriers() {
        assertEquals("[[0,0], [0,1], [0,2], [0,3], [0,4], [0,5], [0,6], " +
                        "[0,7], [0,8], [1,0], [1,1], [1,5], [1,6], [1,7], " +
                        "[1,8], [2,0], [2,6], [2,8], [3,0], [3,1], [3,5], " +
                        "[3,7], [3,8], [4,0], [4,3], [4,4], [4,8], [5,0], " +
                        "[5,5], [5,6], [5,8], [6,0], [6,8], [7,0], [7,3], " +
                        "[7,5], [7,5], [7,7], [7,7], [7,8], [8,0], [8,1], " +
                        "[8,2], [8,3], [8,4], [8,5], [8,6], [8,7], [8,8]]",
                board.getBarriers().toString());
    }

    @Test
    public void shouldWork_isBarrierAt_point() {
        assertEquals(true, board.isBarrierAt(pt(1, 1)));
        assertEquals(false, board.isBarrierAt(pt(5, 1)));
    }

    @Test
    public void shouldWork_getBlasts() {
        assertEquals("[[5,2], [6,2], [7,2]]",
                board.getBlasts().toString());
    }

    @Test
    public void shouldWork_isBlastAt_point() {
        assertEquals(true, board.isBlastAt(pt(7, 2)));
        assertEquals(false, board.isBlastAt(pt(7, 3)));
    }

    @Test
    public void shouldWork_getHero() {
        assertEquals("[1,4]", board.getHero().toString());
        assertEquals("[0,0]", board("☺").getHero().toString());
        assertEquals("[0,0]", board("☻").getHero().toString());
        assertEquals("[0,0]", board("Ѡ").getHero().toString());
        assertEquals(null, board("&").getHero());
    }

    @Test
    public void shouldWork_isHeroAt_point() {
        assertEquals(true, board.isHeroAt(pt(1, 4)));
        assertEquals(false, board.isHeroAt(pt(1, 5)));

        assertEquals(true, board("☺").isHeroAt(pt(0, 0)));
        assertEquals(true, board("☻").isHeroAt(pt(0, 0)));
        assertEquals(true, board("Ѡ").isHeroAt(pt(0, 0)));
        assertEquals(false, board("&").isHeroAt(pt(0, 0)));
    }

    @Test
    public void shouldWork_getPotions() {
        assertEquals("[[1,7], [2,6], [3,5], [4,4], [7,3], [7,5], [7,7]]",
                board.getPotions().toString());
    }

    @Test
    public void shouldWork_isPotionAt_point() {
        assertEquals(true, board.isPotionAt(pt(2, 6)));
        assertEquals(false, board.isPotionAt(pt(2, 7)));
    }

    @Test
    public void shouldWork_getTreasureBoxes() {
        assertEquals("[[1,5], [1,6], [3,2], [6,3]]",
                board.getTreasureBoxes().toString());
    }

    @Test
    public void shouldWork_isTreasureBoxAt_point() {
        assertEquals(true, board.isTreasureBoxAt(pt(3, 2)));
        assertEquals(false, board.isTreasureBoxAt(pt(3, 3)));
    }

    @Test
    public void shouldWork_getFutureBlasts() {
        assertEquals("[[2,7]]",
                board.getFutureBlasts().toString());
    }

    @Test
    public void shouldWork_getFutureBlasts_outOfField() {
        board = board(
                "1      1" +
                "        " +
                "        " +
                "        " +
                "        " +
                "        " +
                "        " +
                "1      1");

        assertEquals("[[1,0], [2,0], [3,0], " +
                        "[0,1], [0,2], [0,3], " +
                        "[1,7], [2,7], [3,7], " +
                        "[0,6], [0,5], [0,4], " +
                        "[6,0], [5,0], [4,0], " +
                        "[7,1], [7,2], [7,3], " +
                        "[6,7], [5,7], [4,7], " +
                        "[7,6], [7,5], [7,4]]",
                board.getFutureBlasts().toString());
    }

    @Test
    public void shouldWork_isFutureBlastAt_point() {
        assertEquals(true, board.isFutureBlastAt(pt(2, 7)));
        assertEquals(false, board.isFutureBlastAt(pt(2, 8)));
    }

    @Test
    public void shouldWork_getGhosts() {
        assertEquals("[[1,1], [1,2], [3,1], [5,6]]",
                board.getGhosts().toString());
    }

    @Test
    public void shouldWork_isGhostAt_point() {
        assertEquals(true, board.isGhostAt(pt(1, 2)));
        assertEquals(false, board.isGhostAt(pt(1, 3)));
    }

    @Test
    public void shouldWork_countNear() {
        assertEquals(0, board.countNear(0, 0, Element.GHOST));
        assertEquals(2, board.countNear(2, 1, Element.GHOST));
        assertEquals(1, board.countNear(4, 1, Element.GHOST));

        assertEquals(2, board.countNear(1, 7, Element.WALL));
        assertEquals(2, board.countNear(1, 1, Element.WALL));
        assertEquals(2, board.countNear(7, 7, Element.WALL));
        assertEquals(2, board.countNear(7, 1, Element.WALL));
        assertEquals(1, board.countNear(1, 6, Element.WALL));
    }

    @Test
    public void shouldWork_countNear_point() {
        assertEquals(0, board.countNear(pt(0, 0), Element.GHOST));
        assertEquals(2, board.countNear(pt(2, 1), Element.GHOST));
        assertEquals(1, board.countNear(pt(4, 1), Element.GHOST));

        assertEquals(2, board.countNear(pt(1, 7), Element.WALL));
        assertEquals(2, board.countNear(pt(1, 1), Element.WALL));
        assertEquals(2, board.countNear(pt(7, 7), Element.WALL));
        assertEquals(2, board.countNear(pt(7, 1), Element.WALL));
        assertEquals(1, board.countNear(pt(1, 6), Element.WALL));
    }

    @Test
    public void shouldWork_isAt() {
        assertEquals(true, board.isAt(3, 1, Element.GHOST));
        assertEquals(false, board.isAt(2, 1, Element.GHOST));

        assertEquals(true, board.isAt(3, 1, Element.HERO_POTION, Element.GHOST));
        assertEquals(false, board.isAt(2, 1, Element.HERO_POTION, Element.GHOST));
    }

    @Test
    public void shouldWork_isAt_point() {
        assertEquals(true, board.isAt(pt(3, 1), Element.GHOST));
        assertEquals(false, board.isAt(pt(2, 1), Element.GHOST));

        assertEquals(true, board.isAt(pt(3, 1), Element.HERO_POTION, Element.GHOST));
        assertEquals(false, board.isAt(pt(2, 1), Element.HERO_POTION, Element.GHOST));
    }

    @Test
    public void shouldWork_isNear() {
        assertEquals(true, board.isNear(1, 1, Element.WALL));
        assertEquals(false, board.isNear(5, 5, Element.WALL));
    }

    @Test
    public void shouldWork_isNear_point() {
        assertEquals(true, board.isNear(pt(1, 1), Element.WALL));
        assertEquals(false, board.isNear(pt(5, 5), Element.WALL));
    }

    @Test
    public void shouldWork_getWalls() {
        assertEquals("[[0,0], [0,1], [0,2], [0,3], [0,4], [0,5], [0,6], [0,7], [0,8], " +
                "[1,0], [1,8], [2,0], [2,8], [3,0], [3,8], [4,0], [4,8], [5,0], [5,8], " +
                "[6,0], [6,8], [7,0], [7,8], [8,0], [8,1], [8,2], [8,3], [8,4], [8,5], " +
                "[8,6], [8,7], [8,8]]",
                board.getWalls().toString());
    }

    @Test
    public void shouldWork_isWallAt_point() {
        assertEquals(true, board.isWallAt(pt(0, 1)));
        assertEquals(false, board.isWallAt(pt(1, 2)));
    }

    @Test
    public void shouldWork_isGameOver() {
        assertEquals(false, board.isGameOver());
        assertEquals(true, board("Ѡ").isGameOver());
    }

    @Test
    public void shouldWork_getAllPerks() {
        board = board(
                "#cr" +
                "#i+" +
                "#TA");

        assertEquals("[[1,0], [1,1], [1,2], [2,0], [2,1], [2,2]]",
                board.getPerks().toString());
    }

    @Test
    public void shouldWork_isPerkAt_point() {
        board = board(
                "#cr" +
                "#i+" +
                "#TA");

        assertEquals(true, board.isPerkAt(pt(1, 0)));
        assertEquals(false, board.isPerkAt(pt(0, 0)));
    }

    @Test
    public void performance_getFutureBlasts() {
        // about 0.7 sec
        // given
        int ticks = 1000;

        // when
        List<Point> blasts = null;
        // парсинг каждый раз с новой бордой
        for (int i = 0; i < ticks / 2; i++) {
            board = board(
                    "☼☼☼☼☼1  ☼☼☼☼☼☼☼☼  1☼☼☼☼☼" +
                    "☼☼☼☼11  ☼1☼☼☼☼1☼  11☼☼☼☼" +
                    "☼☼☼  1  ☼ 1111 ☼  1  ☼☼☼" +
                    "☼☼ 1 1  ☼      ☼  1 1 ☼☼" +
                    "☼1   2  ☼  ☼☼  ☼  2   1☼" +
                    "111122  ☼      ☼  221111" +
                    "      33        33      " +
                    "      33        33      " +
                    "☼☼☼☼☼☼  2      2  ☼☼☼☼☼☼" +
                    "☼1       112211       1☼" +
                    "☼☼1      1    1      1☼☼" +
                    "☼☼1 ☼    2 ☼☼ 2    ☼ 1☼☼" +
                    "☼☼1 ☼    2 ☼☼ 2    ☼ 1☼☼" +
                    "☼☼1      1    1      1☼☼" +
                    "☼1       112211       1☼" +
                    "☼☼☼☼☼☼  2      2  ☼☼☼☼☼☼" +
                    "      33        33      " +
                    "      33        33      " +
                    "111122  ☼      ☼  221111" +
                    "☼1   2  ☼  ☼☼  ☼  2   1☼" +
                    "☼☼ 1 1  ☼      ☼  1 1 ☼☼" +
                    "☼☼☼  1  ☼ 1111 ☼  1  ☼☼☼" +
                    "☼☼☼☼11  ☼1☼☼☼☼1☼  11☼☼☼☼" +
                    "☼☼☼☼☼1  ☼☼☼☼☼☼☼☼  1☼☼☼☼☼");
            blasts = board.getFutureBlasts();
        }

        // парсинг на старой борде
        for (int i = 0; i < ticks / 2; i++) {
            blasts = board.getFutureBlasts();
        }

        // then
        assertEquals("[[0,6], [0,7], [0,17], [0,16], [2,4], [3,4], [4,4], [1,6], [1,7], [2,9], [3,9], [4,9], [2,14], [3,14], [4,14], [1,17], [1,16], [2,19], [3,19], [4,19], [2,6], [2,7], [2,4], [2,3], [3,10], [4,10], [5,10], [2,9], [3,11], [3,12], [3,13], [4,13], [5,13], [2,14], [2,19], [2,20], [2,17], [2,16], [4,3], [2,3], [3,4], [3,2], [3,6], [3,7], [3,4], [3,19], [3,17], [3,16], [4,20], [2,20], [3,21], [3,19], [4,2], [4,3], [4,4], [4,21], [4,20], [4,19], [6,0], [7,0], [6,1], [7,1], [6,2], [7,2], [4,2], [3,2], [6,3], [7,3], [4,3], [6,20], [7,20], [4,20], [6,21], [7,21], [4,21], [3,21], [6,22], [7,22], [6,23], [7,23], [9,2], [9,3], [9,4], [8,9], [7,9], [6,9], [9,8], [9,7], [9,6], [10,10], [11,10], [12,10], [8,10], [7,10], [6,10], [10,13], [11,13], [12,13], [8,13], [7,13], [6,13], [8,14], [7,14], [6,14], [9,15], [9,16], [9,17], [9,21], [9,20], [9,19], [9,2], [10,3], [10,4], [10,5], [10,10], [10,11], [10,12], [10,8], [10,7], [10,6], [10,15], [10,16], [10,17], [10,13], [10,12], [10,11], [9,21], [10,20], [10,19], [10,18], [11,3], [11,20], [12,3], [12,20], [14,2], [13,3], [13,4], [13,5], [13,10], [13,11], [13,12], [13,8], [13,7], [13,6], [13,15], [13,16], [13,17], [13,13], [13,12], [13,11], [14,21], [13,20], [13,19], [13,18], [14,2], [14,3], [14,4], [15,9], [16,9], [17,9], [14,8], [14,7], [14,6], [15,10], [16,10], [17,10], [13,10], [12,10], [11,10], [15,13], [16,13], [17,13], [13,13], [12,13], [11,13], [15,14], [16,14], [17,14], [14,15], [14,16], [14,17], [14,21], [14,20], [14,19], [17,0], [16,0], [17,1], [16,1], [19,2], [20,2], [17,2], [16,2], [19,3], [17,3], [16,3], [19,20], [17,20], [16,20], [19,21], [20,21], [17,21], [16,21], [17,22], [16,22], [17,23], [16,23], [19,2], [19,3], [19,4], [19,21], [19,20], [19,19], [21,3], [19,3], [20,4], [20,2], [20,6], [20,7], [20,4], [20,19], [20,17], [20,16], [21,20], [19,20], [20,21], [20,19], [21,6], [21,7], [21,4], [21,3], [20,10], [19,10], [18,10], [21,9], [20,11], [20,12], [20,13], [19,13], [18,13], [21,14], [21,19], [21,20], [21,17], [21,16], [21,4], [20,4], [19,4], [22,6], [22,7], [21,9], [20,9], [19,9], [21,14], [20,14], [19,14], [22,17], [22,16], [21,19], [20,19], [19,19], [23,6], [23,7], [23,17], [23,16]]",
                blasts.toString());

    }
}
