package com.joewandy.mzmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSparse;

public class TestMatlabMatFile {
	
	public static void main(String[] args) {

		String name = "sparsemat";
		int n = 10;
		int m = 3;
		int nzMax = 30; // no of. non-zero elements to preallocate
		int[] dims = {n, m};
		MLSparse sparse = new MLSparse(name, dims, 0, nzMax);
		sparse.set(10.0, 1, 2);
		sparse.set(14.0, 2, 2);
		
		final Collection<MLArray> output = new ArrayList<MLArray>();
		output.add(sparse);
		final MatFileWriter writer = new MatFileWriter();
		try {
			writer.write("/home/joewandy/Project/mixture_model/test.mat", output);
			System.out.println("Written to test.mat");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}