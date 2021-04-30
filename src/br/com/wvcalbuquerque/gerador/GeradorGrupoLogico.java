//GERADOR DE ALR'S

package br.com.wvcalbuquerque.gerador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GeradorGrupoLogico {

	private static JComboBox comboSistema;
	private static JTextArea textAreaEntrada;
	private static JTextArea textAreaSaida;
	private static Map<String, String> mapeamentoBase;

	public static void main(String[] args) {
		
		JFrame frame = new JFrame("Gerador de Grupo Lógico");
		frame.setLocation(350, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
	    JPanel panelSuperior = new JPanel();
	    JPanel panelInferior = new JPanel();
	    
	    frame.getContentPane().add(panelSuperior, "North");
	    frame.getContentPane().add(panelInferior, "South");

	    textAreaEntrada = new JTextArea(30, 40);	    
	    textAreaEntrada.setLineWrap(true);
	    
	    textAreaSaida = new JTextArea(30, 40);	    	    
	    textAreaSaida.setLineWrap(true);	    
	    
	    comboSistema = new JComboBox();
		
		String filePath = "C:\\Bases\\SISTEMAS.txt";
		File file = new File(filePath);
        
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			Object[] lines = br.lines().toArray();
			
			for(int i = 0; i < lines.length; i++){
		        String line = lines[i].toString();
		        comboSistema.addItem(line);
		    }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}    
	    
	    JButton btnGerar = new JButton("Gerar");
	    btnGerar.addActionListener(
	    		new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							gerar();
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
	    );
	    
	    panelSuperior.add(new JScrollPane(textAreaEntrada));
	    panelSuperior.add(new JScrollPane(textAreaSaida));
	    
	    panelInferior.add(comboSistema);
	    panelInferior.add(btnGerar);
	    
	    frame.pack();
	    frame.setVisible(true);
	  }
	
	private static void gerar() throws Exception {
		mapeamentoBase = new HashMap<String, String>();
		Map<String, Set<String>> mapeamentoGrupoLogico = new HashMap<String, Set<String>>();
		List<String> tabelasNaoMapeadas = new ArrayList<String>();
		try {
			mapearArquivoBase();
			String entrada = textAreaEntrada.getText();
			String[] linhaStrings = entrada.split("\\n");
			for (String linha : linhaStrings) {
				tabelasNaoMapeadas.addAll(mapearLinhaArquivoEntrada(linha, mapeamentoBase, mapeamentoGrupoLogico));
			}
			String saida = processarSaida(mapeamentoGrupoLogico, tabelasNaoMapeadas);
			textAreaSaida.setText(saida);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Erro na abertura do arquivo base: " + e.getMessage(), 
					"Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void mapearArquivoBase() throws Exception {
		String sistema = comboSistema.getSelectedItem().toString();
		String arquivoBase = getCaminhoPastaBase() + sistema + ".txt";
		mapearArquivoBase(mapeamentoBase, arquivoBase);
	}
	
	private static String processarSaida(Map<String, Set<String>> mapeamentoGrupoLogico, List<String> tabelasNaoMapeadas) {
		StringBuilder linha = new StringBuilder();
		int indice = 1;
		for (String grupoLogico : mapeamentoGrupoLogico.keySet()) {
		    linha.append(indice);
		    linha.append(". ");
			linha.append(grupoLogico);
			linha.append(" (");
			for (String tabela : mapeamentoGrupoLogico.get(grupoLogico)) {
				linha.append(tabela);
				linha.append(" + ");
			}
			linha.delete(linha.length() - 3, linha.length());
			linha.append(")\n");
			indice++;
		}
		if (!tabelasNaoMapeadas.isEmpty()) {
			linha.append("\nTabelas não mapeadas:\n\n");
			for (String tabelaNaoMapeada : tabelasNaoMapeadas) {
				linha.append(indice);
				linha.append(". ");
				linha.append(tabelaNaoMapeada);
				linha.append("\n");
				indice++;
			}
		}
		return linha.toString();
	}
	
	public static void mapearArquivoBase(
			Map<String, String> mapeamentoBase,
			String arquivoBase) throws IOException, URISyntaxException {
		System.out.println("Mapeando arquivo base...\n");
		long inicio = System.currentTimeMillis();
		try { 
			InputStream arq = new FileInputStream(arquivoBase);
			BufferedReader lerArq = new BufferedReader(new InputStreamReader(arq, "UTF-8"));
			String linha = lerArq.readLine();
			while (linha != null) { 
				mapearLinhaArquivoBase(linha, mapeamentoBase);
				linha = lerArq.readLine();
			} 
			arq.close(); 
		} catch (IOException e) { 
			System.err.printf("Erro na abertura do arquivo base: %s.\n", e.getMessage()); 
			throw e;
		}
		System.out.println("Arquivo base mapeado. Tempo: " + (System.currentTimeMillis() - inicio) + "\n");
	}
	
	public static void mapearArquivoBase(
			Map<String, String> mapeamentoBase,
			File arquivoBase) throws IOException, URISyntaxException {
		System.out.println("Mapeando arquivo base...\n");
		long inicio = System.currentTimeMillis();
		try { 
			FileReader arq = new FileReader(arquivoBase); 
			BufferedReader lerArq = new BufferedReader(arq); 
			String linha = lerArq.readLine();
			while (linha != null) { 
				mapearLinhaArquivoBase(linha, mapeamentoBase);
				linha = lerArq.readLine();
			} 
			arq.close(); 
		} catch (IOException e) { 
			System.err.printf("Erro na abertura do arquivo base: %s.\n", e.getMessage()); 
			throw e;
		}
		System.out.println("Arquivo base mapeado. Tempo: " + (System.currentTimeMillis() - inicio) + "\n");
	}

	public static void mapearLinhaArquivoBase(String linha, Map<String, String> mapeamentoBase) {
		String[] tabelaGrupoLogico = linha.split(";");
		if (tabelaGrupoLogico.length == 2) {
			String tabela = tabelaGrupoLogico[0];
			String grupoLogico = tabelaGrupoLogico[1];
			if (!mapeamentoBase.containsKey(tabela)) {
				mapeamentoBase.put(tabela, grupoLogico);
			}
		}
	}
	
	public static List<String> mapearLinhaArquivoEntrada(String linha,
			Map<String, String> mapeamentoBase,
			Map<String, Set<String>> mapeamentoGrupoLogico) {
		List<String> tabelasNaoMapeadas = new ArrayList<String>();
		String tabela = linha;
		String grupoLogico = mapeamentoBase.get(tabela);
		if (grupoLogico == null) {
		    System.out.println(tabela + "' não estão mapeada no arquivo base.");
		    tabelasNaoMapeadas.add(tabela);
		} else {
		    if (mapeamentoGrupoLogico.containsKey(grupoLogico)) {
		        Set<String> tabelasGrupoLogico = mapeamentoGrupoLogico.get(grupoLogico);
		        tabelasGrupoLogico.add(tabela);
		        mapeamentoGrupoLogico.remove(grupoLogico);
		        mapeamentoGrupoLogico.put(grupoLogico, tabelasGrupoLogico);
		    } else {
		        Set<String> tabelasGrupoLogico = new HashSet<String>();
		        tabelasGrupoLogico.add(tabela);
		        mapeamentoGrupoLogico.put(grupoLogico, tabelasGrupoLogico);
		    }
		}
		return tabelasNaoMapeadas;
	}
	
	private static String getCaminhoPastaBase() throws Exception {
		String caminho = null;
		Properties properties = getProp();
		caminho = properties.getProperty("prop.pasta.base");
		return caminho;
	}
	
	private static Properties getProp() throws Exception {
		Properties props = new Properties();
		InputStream resourceAsStream = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("br/com/wvcalbuquerque/gerador/properties/base.properties");
		if (resourceAsStream == null) {
			throw new Exception("Arquivo de propriedades não encontrado.");
		}
		props.load(resourceAsStream);
		return props;
	}

	
}
