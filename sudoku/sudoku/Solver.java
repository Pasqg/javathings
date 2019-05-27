package sudoku;

class Monitor extends Thread {
	public Solver s;
	
	public Monitor(Solver solver) {
		s = solver;
	}
	
	public void run() {
		while (s.solved == null) {
			if (s.current != null) {
				System.out.println("STEP: "+s.step+"\n-----------------------");
				for (int i = 0; i < 9; i++) {
					for (int k = 0; k < 9; k++) {
						System.out.print(s.current[i][k]+" ");
						if ((k+1)%3 == 0) System.out.print("| "); 
					}
					if ((i+1)%3 == 0) System.out.print("\n-----------------------");
					System.out.print("\n");
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("\n\nSolved in "+s.step+" step");
		
		for (int i = 0; i < 9; i++) {
			for (int k = 0; k < 9; k++) {
				System.out.print(s.solved[i][k]+" ");
				if ((k+1)%3 == 0) System.out.print("| "); 
			}
			if ((i+1)%3 == 0) System.out.print("\n-----------------------");
			System.out.print("\n");
		}
	}
}

public class Solver {
	public byte[][] solved;
	public byte[][] current;
	public int currentStep=0;
	public int step=0;
	
	public boolean solve(byte[][] sudoku, int k, int i) {
		current = sudoku;
		currentStep++;
		//System.out.println(step);
		//posizionamento
		while (sudoku[k][i] != 0) {
			i++;
			if (i >= 9) {
				k++;
				if (k >= 9) {
					solved = sudoku;
					if (step == 0) step = currentStep;
					return true;
				}
				i = i%9;
			}
		}

		//possibilita di scelta
		boolean[] possibilities= new boolean[9];
		for (int p = 0; p < 9; p++) 
			possibilities[p] = true;
		for (int p = 0; p < 9; p++) {
			if (sudoku[k][p]!=0) possibilities[sudoku[k][p]-1] = false;
			if (sudoku[p][i]!=0) possibilities[sudoku[p][i]-1] = false;
			if (sudoku[(k/3)*3+p/3][(i/3)*3+p%3]!=0) possibilities[sudoku[(k/3)*3+p/3][(i/3)*3+p%3]-1] = false;
			
		}
		
		//for (int p = 0; p < 9; p++)	System.out.println(possibilities[p]);
		byte p = 0;
		while (p < 9 && !possibilities[p]) {
			p++;
		}
		//System.out.println(p);
		if (p >= 9) return false;
		else {
			while (p<9) {
				//scelgo e passo alla casella successiva
				byte[][] sudoku2 = new byte[9][9];
				for (int q = 0; q < 9; q++) {
					for (int s = 0; s < 9; s++) {
						sudoku2[q][s] = sudoku[q][s];
					}
				}
				sudoku2[k][i] = (byte)(p+1);
				current = sudoku2;
				//System.out.println(sudoku2[k][i]+" "+k+" "+i+" "+(byte)(p+1));
				int f = i;
				int g = k;
				f++;
				if (f >= 9) {
					g++;
					if (g >= 9) {
						solved = sudoku2;
						if (step == 0) step = currentStep;
						return true;
					}
					f = f%9;
				}
				boolean b = solve(sudoku2,g,f);
				currentStep--;
				if (!b) { 
					possibilities[p] = false;
					while (p < 9 && !possibilities[p]) {
						p++;
					}
				}
				else return true;
			}
			return false;
		}
	}
	
	public static void main(String args[]) {
		byte[][] sudoku={{6,0,0,0,0,0,0,0,4},
						 {0,8,9,0,0,0,3,1,0},
						 {0,7,0,2,0,9,0,5,0},
						 {0,0,4,0,0,0,8,0,0},
						 {0,0,0,0,0,0,9,0,0},
						 {0,0,2,0,0,0,1,0,0},
						 {0,0,0,0,0,0,0,3,0},
						 {0,0,0,0,0,0,2,8,0},
						 {0,0,0,0,0,0,0,0,5}};

		Solver s = new Solver();
		//Thread t = new Monitor(s);
		//t.start();
		long start = System.currentTimeMillis();
		boolean b = s.solve(sudoku,0,0);
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		/*try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
