package sudoku;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class Checker extends Thread {
	private JTextField txt[];
	
	public Checker(JTextField[] jtxt) {
		txt = jtxt;
	}
	
	public void run() {
		while(true) {
			for (int i = 0; i < txt.length; i++) {
				try {
					int val = Integer.parseInt(txt[i].getText());
					if (val < 1 || val > 9) txt[i].setText("");
				}
				catch (Exception e) {
					txt[i].setText("");
				}
			}
		}
	}
}

public class ui extends JFrame {

	private byte[][] unsolved;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ui frame = new ui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ui() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 431, 520);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		int offsety = 0;
		int offsetx = 0;
		final JTextField textField[] = new JTextField[81];
		for (int i = 0; i < 9; i++) {
			offsety += 8;
			if ((i)%3 == 0) offsety += 8;
			for (int k = 0; k < 9; k++) {
				offsetx += 8;
				if ((k)%3 == 0) offsetx += 8;
				
				textField[i*9+k] = new JTextField();
				textField[i*9+k].setHorizontalAlignment(SwingConstants.CENTER);
				textField[i*9+k].setFont(new Font("Tahoma", Font.PLAIN, 20));
				textField[i*9+k].setBounds(10+k*32+offsetx, 11+i*32+offsety, 32, 32);
				contentPane.add(textField[i*9+k]);
				textField[i*9+k].setColumns(10);
			}
			offsetx = 0;
		}
		
		JButton btnSolve = new JButton("Solve");
		btnSolve.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				byte[][] sudoku = new byte[9][9];
				for (int i = 0; i < 9; i++) {
					for (int k = 0; k < 9; k++) {
						try {
							int val = Integer.parseInt(textField[i*9+k].getText());
							if (val < 1 || val > 9) {
								sudoku[i][k] = (byte)0;
							}
							else sudoku[i][k] = (byte)val;
						}
						catch (Exception e) {
							sudoku[i][k] = (byte)0;
						}
					}
				}
				unsolved = sudoku;
				Solver s = new Solver();
				s.solve(sudoku,0,0);
				//System.out.println(s.solve(sudoku,0,0));
				for (int i = 0; i < 9; i++) {
					for (int k = 0; k < 9; k++) {
						textField[i*9+k].setText(""+s.solved[i][k]);
					}
				}
			}
		});
		btnSolve.setBounds(166, 457, 89, 23);
		contentPane.add(btnSolve);
		
		JButton btnReset = new JButton("Reset");
		btnReset.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				for (int i = 0; i < textField.length; i++) {
					textField[i].setText("");
				}
			}
		});
		btnReset.setBounds(10, 457, 89, 23);
		contentPane.add(btnReset);
		
		JButton btnUnsolve = new JButton("Unsolve");
		btnUnsolve.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				for (int i = 0; i < 9; i++) {
					for (int k = 0; k < 9; k++) {
						if (unsolved[i][k] > 0 && unsolved[i][k] <= 9) textField[i*9+k].setText(""+unsolved[i][k]);
						else textField[i*9+k].setText("");
					}
				}
			}
		});
		btnUnsolve.setBounds(326, 457, 89, 23);
		contentPane.add(btnUnsolve);
		
		Thread t = new Checker(textField);
		//t.start();
	}
}
