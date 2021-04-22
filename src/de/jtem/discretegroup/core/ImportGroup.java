/*
 * Created on Mar 5, 2009
 *
 */
package de.jtem.discretegroup.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.ResourceClass;

public class ImportGroup {

	private final static int RAW_GENERATORS = 0;
	private final static int WEEKS_GEN = 1;
	private final static int WEEKS_MAT = 2;
	private final static int PSL2C_MAT = 3;
	public static DiscreteGroup initFromFile(String filename, int metric)	{
			File file = new File(filename);
			return initFromFile(file, metric);
	}
	
	public static DiscreteGroup initFromResource(String resourceName)	{
		return initFromResource(resourceName, Pn.PROJECTIVE);
	}
	public static DiscreteGroup initFromResource(String resourceName, int sig)	{
		InputStream is = ResourceClass.class.getResourceAsStream(resourceName);
		BufferedReader bf = new BufferedReader(new InputStreamReader(is));
		return initFromBufferedReader(bf, resourceName, sig);
	}
	

	public static DiscreteGroup initFromFile(String filename)	{
		return initFromFile(filename, Pn.PROJECTIVE);
	}
	
	public static DiscreteGroup initFromFile(File file, int metric)	{
		BufferedReader fin = null;
		try {
			fin = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return initFromBufferedReader(fin, file.getName(), metric);
	}

	public static DiscreteGroup initFromBufferedReader(BufferedReader fin, String filename, int metric)	{
			int type = RAW_GENERATORS;
			boolean transpose = false;
			if (filename.endsWith(".gen")) {
				type = WEEKS_GEN;
				transpose = true;
			}
			else if (filename.endsWith(".mat")) {
				type = WEEKS_MAT;
				transpose = true;
			} else if (filename.endsWith(".psl2c")) {
				type = PSL2C_MAT;
//				transpose = true;
			}

			String ss;
			double[] mat = new double[16];
			int count = 0, gcount = 0;
			Vector<DiscreteGroupElement> matlist = new Vector<DiscreteGroupElement>();
			//int metric = Pn.EUCLIDEAN;
			boolean firsttime = true, readRadius = false;
			try {
				while ( (ss = fin.readLine()) != null)	{
					StringTokenizer st = new java.util.StringTokenizer(ss);
					if (!readRadius && type == WEEKS_GEN) {
						Double.parseDouble(st.nextToken());
						readRadius = true;
					}
					while (st.hasMoreTokens())	{
						mat[count] = Double.parseDouble(st.nextToken());
						count++;
						if ((count % 16) == 0)	{	// read one matrix
							if (transpose)	mat = Rn.transpose(null, mat);
							if (firsttime && metric == Pn.PROJECTIVE)	{	// first matrix!
								if (mat[12] == 0.0 && mat[13] == 0.0 && mat[14] == 0.0 && mat[15] == 1.0) metric = Pn.EUCLIDEAN;
								else {		// try to figure out the curvature;
									double[] row1 = new double[4];
									System.arraycopy(mat, 0, row1, 0, 4);
									if (Rn.innerProduct(row1, row1) > 1.0001) metric = Pn.HYPERBOLIC;
									else metric = Pn.ELLIPTIC;
								}
								firsttime = false;
							}
							count = 0;
							DiscreteGroupElement gen = new DiscreteGroupElement(metric, mat);
							if (type != WEEKS_MAT) {
								gen.setWord(DiscreteGroupUtility.genNames[gcount]);	
								gen.setColorIndex(gcount);
							}
							matlist.add(gen);
							matlist.add(gen.getInverse());
							gcount++;
							mat = new double[16];
						}
					}
				}
				fin.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			DiscreteGroup dg = new DiscreteGroup();
			DiscreteGroupElement[] garray = new DiscreteGroupElement[0];
			for (DiscreteGroupElement dge : garray)	{
				if (dge.getArray()[15] < 0)	{
					if (metric == Pn.ELLIPTIC) dge.setMapsToNegativeW(true);
					else dge.setArray(Rn.times(null, -1, dge.getArray()));
				}
			}
			garray = matlist.toArray(garray);
			// normalize matrices as much as possible to avoid mapping onto w < 0
			if (type == WEEKS_MAT) {
				dg.setElementList(garray);
				dg.setGenerators(null);
			}
			else dg.setGenerators(garray);
			dg.setDimension(3);
			dg.setMetric(metric);
			return dg;
	}
}
