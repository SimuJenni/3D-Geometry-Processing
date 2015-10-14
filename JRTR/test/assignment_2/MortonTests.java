package assignment_2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import assignment2.MortonCodes;

public class MortonTests {
	
	private int level;
    private long hash, parent, nbr_plus_x, nbr_plus_y, nbr_plus_z, nbr_minus_x,
    nbr_minus_y, nbr_minus_z, vertexHash;

	@Before
    public void setUp() {
		//example of a level 4 morton code
		hash = 		0b1000101000100;
		level = 4;
		
		//the hashes of its parent and neighbors
		parent = 		0b1000101000;
		nbr_plus_x = 	0b1000101100000;
		nbr_plus_y =   0b1000101000110;
		nbr_plus_z =   0b1000101000101;
		
		nbr_minus_x = 	0b1000101000000;
		nbr_minus_y =  -1; //invalid: the vertex lies on the boundary and an underflow should occur
		nbr_minus_z =  0b1000100001101;
		
		//example of a a vertex morton code in a multigrid of
		//depth 4. It lies on the level 3 and 4 grids
		vertexHash = 0b1000110100000;
    }

	@Test
	public void testParent() {
		assertEquals(parent, MortonCodes.parentCode(hash));
	}
	
	@Test
	public void test_nbr_plus_x() {
		assertEquals(nbr_plus_x, MortonCodes.nbrCode(hash, level, 0b100));
	}
	
	@Test
	public void test_nbr_plus_y() {
		assertEquals(nbr_plus_y, MortonCodes.nbrCode(hash, level, 0b010));
	}
	
	@Test
	public void test_nbr_plus_z() {
		assertEquals(nbr_plus_z, MortonCodes.nbrCode(hash, level, 0b001));
	}
	
	@Test
	public void test_nbr_minus_x() {
		assertEquals(nbr_minus_x, MortonCodes.nbrCodeMinus(hash, level, 0b100));
	}
	
	@Test
	public void test_nbr_minus_y() {
		assertEquals(nbr_minus_y, MortonCodes.nbrCodeMinus(hash, level, 0b010));
	}
	
	@Test
	public void test_nbr_minus_z() {
		assertEquals(nbr_minus_z, MortonCodes.nbrCodeMinus(hash, level, 0b001));
	}
	
	@Test
	public void test_overflow() {
		assertEquals(-1, MortonCodes.nbrCode(0b100, 1, 0b100));
	}

	@Test
	public void testDepth() {
		assertEquals(5, MortonCodes.getTreeDepth(0b1111111111111111));
		assertEquals(4, MortonCodes.getTreeDepth(0b1110110111101));
		assertEquals(1, MortonCodes.getTreeDepth(0b1100));
		assertEquals(2, MortonCodes.getTreeDepth(0b1101101));
	}
	
	@Test
	public void testLevelTransform() {
		assertEquals(0b100000, MortonCodes.transformToLevel(0b100, 1, 2));
		assertEquals(0b000010000, MortonCodes.transformToLevel(0b010, 2, 3));
		assertEquals(0b000000000001, MortonCodes.transformToLevel(0b001, 4, 4));
	}
	
	@Test
	public void testVertexOnLevelXGrid() {
		assertTrue(MortonCodes.isVertexOnLevelXGrid(vertexHash, 3, 4));
		assertTrue(MortonCodes.isVertexOnLevelXGrid(vertexHash, 4, 4));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 2, 4));
		assertFalse(MortonCodes.isVertexOnLevelXGrid(vertexHash, 1, 4));
	}
	
	@Test
	public void testCellOnLevelXGrid() {
		assertTrue(MortonCodes.isCellOnLevelXGrid(hash, 4));
		assertFalse(MortonCodes.isCellOnLevelXGrid(hash, 3));
	}
}
