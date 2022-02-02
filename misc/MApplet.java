//
//  RCApplet.java
//  RealCalc2
//
//  Created by Will Thimbleby on 29/06/2005.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.Graphics.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.geom.*;

public class MApplet extends Applet
{
	DrawPanel drawPanel;
	PrintThread printThread = null;
	JScrollPane scrollPane;
	JTextPane textPane;
	TerminalDoc document;
    
	public class TerminalDoc extends DefaultStyledDocument
	{
		int staticLimit = 0;
		SimpleAttributeSet outputAttr;
		SimpleAttributeSet inputAttr;
		SimpleAttributeSet promptAttr;
		
		public TerminalDoc()
		{
			TabStop stops[] = new TabStop[20];
            for(int i=0;i<20;i++)
				stops[i] = new TabStop(40*i);
            Style style = textPane.getLogicalStyle();
            StyleConstants.setTabSet(style, new TabSet(stops));
            setLogicalStyle(0, style);
		
			outputAttr = new SimpleAttributeSet();
			StyleConstants.setFontFamily(outputAttr, "Courier");
			StyleConstants.setFontSize(outputAttr, 14);
			//StyleConstants.setTabSet(outputAttr, new TabSet(stops));
			
			inputAttr = new SimpleAttributeSet(outputAttr);
			
			StyleConstants.setForeground(outputAttr, new Color(0,0,0));
			StyleConstants.setForeground(inputAttr, new Color(100,56,32));
			//StyleConstants.setTabSet(inputAttr, new TabSet(stops));
			
			promptAttr = new SimpleAttributeSet(inputAttr);
			StyleConstants.setBold(promptAttr, true);
			StyleConstants.setForeground(promptAttr, new Color(0,0,255));
		/*
			try
			{
				PipedInputStream piOut = new PipedInputStream();
				PipedOutputStream poOut = new PipedOutputStream(piOut);
				System.setOut(new PrintStream(poOut, true));
				new ReaderThread(piOut).start();
			}
			catch(Exception e){}*/
		}
		
		public void appendString(String str)
		{
			try {
                StyleConstants.setForeground(outputAttr, new Color(0,0,0));
				super.insertString(getLength(), str, outputAttr);
			}
			catch(Exception e) {
			}
			
			staticLimit = getLength();
			textPane.setCaretPosition(staticLimit);
		}
		
		public void appendString(String str, Color color)
		{
			try {
                StyleConstants.setForeground(outputAttr, color);
				super.insertString(getLength(), str, outputAttr);
			}
			catch(Exception e) {
			}
			
			staticLimit = getLength();
			textPane.setCaretPosition(staticLimit);
		}
		
		public void prompt()
		{
			try {
				super.insertString(getLength(), "\n=> ", promptAttr);
			}
			catch(Exception e) {
			}
			staticLimit = getLength();
			textPane.setCaretPosition(staticLimit);
		}
		
		public void insertString(int offset, String str, AttributeSet a) throws javax.swing.text.BadLocationException
		{
			if(printThread != null && printThread.isAlive()) return;
			
			if(offset < staticLimit)
			{
				offset = getLength();
				textPane.setCaretPosition(getLength());
			}
			
			super.insertString(offset, str, inputAttr);
		}
		
		public void remove(int offs, int len) throws javax.swing.text.BadLocationException
		{
			if(offs < staticLimit) return;
			super.remove(offs, len);
		}
	}
	
	/*
	class ReaderThread extends Thread
	{
		PipedInputStream pi;

		ReaderThread(PipedInputStream pi)
		{
			this.pi = pi;
		}
		
		public void run()
		{
			final byte[] buf = new byte[1024];
			try {
				while (true)
				{
					final int len = pi.read(buf);
					if (len == -1)
					{
						break;
					}
					final String str = new String(buf, 0, len);
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							document.appendString(str);

							// Make sure the last line is always visible
							/*textArea.setCaretPosition(textArea.getDocument().getLength());

							// Keep the text area down to a certain character size
							int idealSize = 1000;
							int maxExcess = 500;
							int excess = textArea.getDocument().getLength() - idealSize;
							if (excess >= maxExcess) {
								textArea.replaceRange("", 0, excess);
							}* /
						}
					});
				}
			} catch (IOException e)
			{
				e.printStackTrace( );
			}
			
			SwingUtilities.invokeLater(new Runnable(){public void run(){document.prompt();}});
		}
	}*/
    
    public void printStringAppend(StringAppend sa, int dataLevel, int depth, boolean asData, Color outColor) throws java.io.IOException
	{
		int d = sa.dataLevel() - dataLevel;
		dataLevel += d;
		//if((MapData.class == getClass() || MapUnion.class == obj.getClass()) || obj.isName())
		{
			while(d-- > 0)
				document.appendString("'");
			while(d++ < -1)
				document.appendString(",");
		}
		
		document.appendString( "\"", outColor);
		document.appendString( sa.value.toString(), outColor);
			
		while(sa.appending.isEmpty() == false)
		{
			document.appendString( sa.computeAppend(), outColor);
		}
		
		document.appendString( "\"", outColor);
	}
    
    public Color colorOfObject(Atom obj)
    {
        try{
            Map style = (Map)obj.getMeta(new StringLiteral("style")).evalForData();
            Map color = (Map)(style != null? style.get(new StringLiteral("color")).evalForData() : null);
            Color outColor;
            if(color != null)
            {
                return new Color(((NumberLiteral)color.get(NumberLiteral.zero).evalForData()).floatValue(), 
                    ((NumberLiteral)color.get(NumberLiteral.one).evalForData()).floatValue(), 
                    ((NumberLiteral)color.get(NumberLiteral.two).evalForData()).floatValue());
            }
        }
        catch(Exception e)
		{}
        
        return new Color(0,0,0);
    }
    
    public void printObject(Atom obj, int dataLevel, int depth, boolean asData, boolean asMeta)
	{
		try{
		
		if(obj == null)
		{
			document.appendString("null");
			return;
		}
		
		if(!asData)
			obj = obj.evalForData();
		
		if(obj == null)
		{
			document.appendString("NULL");
			return;
		}
        
        Color outColor = colorOfObject(obj);
		
		if(obj.isStatic() && MapData.class.isAssignableFrom(obj.getClass()))
		{
			if(!asMeta) document.appendString("`");
		}
		else
		{
			int d = obj.dataLevel() - dataLevel;
			dataLevel += d;
			if((MapData.class == obj.getClass() || MapUnion.class == obj.getClass()) || obj.isName())
			{
				while(d-- > 0)
					document.appendString("'");
				while(d++ < -1)
					document.appendString(",");
			}
		}
		
		if(StringAppend.class == obj.getClass())
		{
			printStringAppend((StringAppend)obj, dataLevel, depth, asData, outColor);
		}
		else if(MapData.class != obj.getClass() && MapUnion.class != obj.getClass())
		{
			document.appendString( obj.toString(), outColor);
		}
		else
		{
			Map ppmo = (Map)obj.getMeta(new StringLiteral("style"));
            if(ppmo != null) ppmo = (Map)ppmo.get(new StringLiteral("format"));
		
			//if(atom.getMeta(MObject.isDataMap) != null) out.print("{");
			document.appendString("[", outColor);
			
			boolean printedKey = false;
			NumberLiteral index = NumberLiteral.zero;
			
			Map env;
			Atom enva = obj.getMeta(StringLiteral.defEnv);
			if(enva != null)
				env = (Map)enva.evalForData();
			else
				env = null;

			if(ppmo != null)
            {
                int kindex = 0;
                Atom key;
                while((key = ppmo.get(new NumberLiteral(kindex))) != null)
                {
                    if(printedKey) document.appendString(" ");
                
                    if(NumberLiteral.class == key.getClass())
                    {
                        long returns = ((NumberLiteral)key).longValue();
                        while(returns-- > 0) document.appendString("\n");
                        for(int dd=0; dd<depth; dd++) document.appendString("\t");
                        printedKey = false;
                    }
                    else
                    {
                        Atom type = ((Map)key).get(NumberLiteral.zero);
                        
                        if(type.equals(new StringLiteral("comment")))
                        {
                            document.appendString("#:");
                            printObject(((Map)key).get(NumberLiteral.one), dataLevel, depth+1, false, false);
                        }
                        else if(type.equals(new StringLiteral("key")))
                        {
                            Atom keyk = ((Map)key).get(NumberLiteral.one);
                            Atom o = ((Map)obj).get(keyk);
                            
                            if(NumberLiteral.class == keyk.getClass())
                            {
                                if(((NumberLiteral)keyk).compareTo(index) != 0)
                                    document.appendString(keyk.toKeyString()+":", colorOfObject(keyk));
                                
                                index = ((NumberLiteral)keyk).add(NumberLiteral.one);
                            }
                            else
                            {
                                //others
                                Map mkeys = ((Map)obj).keys();
                                Atom realkey;
                                int kindex2 = 0;
                                while((realkey = mkeys.get(new NumberLiteral(kindex2))) != null)
                                {
                                    if(keyk.equals(realkey))
                                    {
                                        keyk = realkey;
                                        break;
                                    }
                                    kindex2++;
                                }
                            
                                document.appendString(keyk.toKeyString()+":", colorOfObject(keyk));
                            }
                            
                            printObject(o, dataLevel, depth+1, false, false);
                        }
                        printedKey = true;
                    }
                
                    kindex++;
                }
            }
            else
            {
                //others
                Map mkeys = ((Map)obj).keys();
                Atom key;
                int kindex = 0;
                while((key = mkeys.get(new NumberLiteral(kindex))) != null)
                {
                    Atom o = ((Map)obj).get(key);
                    
                    if(printedKey) document.appendString(" ");
                    
                        if(NumberLiteral.class == key.getClass())
                        {
                            if(((NumberLiteral)key).compareTo(index) != 0)
                                document.appendString(key.toKeyString()+":", colorOfObject(key));
                            
                            index = ((NumberLiteral)key).add(NumberLiteral.one);
                        }
                        else
                        {
                            if(asMeta && (key.equals(StringLiteral.isData) || key.equals(StringLiteral.isName) || key.equals(StringLiteral.defEnv)))
                            { kindex++; printedKey = false; continue; }
                            
                            document.appendString(key.toKeyString()+":", colorOfObject(key));
                        }
                            
                    printObject(o, dataLevel, depth+1, false, false);
                    kindex++;
                    printedKey = true;
                }
			}
			
			//if(atom.getMeta(MObject.isDataMap) != null) out.print("}");
			document.appendString("]", outColor);
		}
		
		}
		catch(Exception e)
		{
			e.printStackTrace( );
		}
	}
	
	class PrintThread extends Thread
	{
		Atom program;
		Writer w;
		
		PrintThread(Atom prgm, Writer w)
		{
			program = prgm;
			this.w = w;
		}

		public void run()
		{
			try{
				printObject(program, 0, 0, false, false);
				document.prompt();
				drawPanel.repaint();
			} catch(Exception e){}
		}
		
		public void close()
		{
			try{
				w.close();
			} catch(Exception e){}
		}
	}
	
	Atom result = null;
	
	public class DrawPanel extends JPanel
	{
		public void paintComponent(Graphics g)
		{
			Graphics2D		g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			Rectangle r = getBounds();
			Rectangle2D rbounds = new Rectangle2D.Float(0f, 0f, (float)r.getWidth()-1, (float)r.getHeight()-1);
			
			GeneralPath gp = null;
			try {
			gp = new GeneralPath();
			
			Map path = (Map)result.evalForData();
			Map mkeys = path.keys();
			Atom key;
			NumberLiteral kindex = NumberLiteral.zero;
			while((key = mkeys.get(kindex)) != null)
			{
				Atom o = path.getData(key);
				
				Map action = (Map)o;
				
				Atom type = action.getData(NumberLiteral.zero);
				if(type.equals("m"))
					gp.moveTo(action.getNumber(NumberLiteral.one).floatValue(),
						action.getNumber(NumberLiteral.two).floatValue());
				else if(type.equals("l"))
					gp.lineTo(action.getNumber(NumberLiteral.one).floatValue(),
						action.getNumber(NumberLiteral.two).floatValue());
				else if(type.equals("c"))
					gp.curveTo(action.getNumber(NumberLiteral.one).floatValue(),
						action.getNumber(NumberLiteral.two).floatValue(),
						action.getNumber(NumberLiteral.three).floatValue(),
						action.getNumber(NumberLiteral.four).floatValue(),
						action.getNumber(NumberLiteral.five).floatValue(),
						action.getNumber(NumberLiteral.six).floatValue());
				else if(type.equals("z"))
					gp.closePath();

				kindex = kindex.add(NumberLiteral.one);
			}
			
			//g2d.setStroke(new BasicStroke());
			}
			catch(Exception e)
			{gp = null;}
			
			if(gp != null || result == null)
			{
				g2d.setColor(Color.white);
				g2d.fill(rbounds);
				g2d.setColor(Color.black);
				g2d.draw(rbounds);
			}
			if(gp != null)
				g2d.draw(gp);
		}
	}
		
	public Action killAction = new AbstractAction("Kill")
	{
		public void actionPerformed(ActionEvent evt)
		{
			printThread.close();
		}
	};
	public Action runAction = new AbstractAction("Run")
	{
		public void actionPerformed(ActionEvent evt)
		{
			try {
				document.insertString(document.getLength(), "\n", document.promptAttr);
				
				BufferedReader br = new BufferedReader(new StringReader(document.getText(document.staticLimit, document.getLength()-document.staticLimit)));

				result = NullLiteral.nullValue;
				result = Parser.parseExpr(br, 1);
				result = result.deepcopy(-1, Misc.rootEnvironment);
//					result = result.evalForDataNoMemo();
				
				PipedInputStream pi = new PipedInputStream();
				PipedOutputStream poOut = new PipedOutputStream(pi);
				
				//new ReaderThread(pi).start();
				printThread = new PrintThread(result, null/*new BufferedWriter(new OutputStreamWriter(poOut))*/);
				printThread.start();
				
				//new ReaderThread(result).start();
				
				/*
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				Misc.printObject(ps, result, 0, 0, true);
			
				appendString(baos.toString());
				prompt();*/
			}
			catch(Exception e) {
				e.printStackTrace( );
				document.prompt();
			}
		}
	};
	
	public void runCode(String code)
	{
		try{
			document.insertString(document.getLength(), code, document.inputAttr);
			runAction.actionPerformed(null);
		}
		catch(Exception e){}
	}
	
	public void init()
	{
		textPane = new JTextPane();
		scrollPane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		document = new TerminalDoc();
		
		textPane.setDocument(document);
		
		InputMap inputMap = textPane.getInputMap();
		KeyStroke kkey = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK);
		inputMap.put(kkey, killAction);
		KeyStroke rkey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.ALT_MASK);
		inputMap.put(rkey, runAction);
		
		setLayout(new BorderLayout());
		add(scrollPane,BorderLayout.CENTER);
		
		//draw panel
		drawPanel = new DrawPanel();
		drawPanel.setPreferredSize(new Dimension(300,300));
		add(drawPanel, BorderLayout.EAST);
		
		Misc.baseURL = getCodeBase();
		Misc m = new Misc();
		
		try {
			document.appendString("/============================================\n");
			document.appendString("|  Welcome to MISC \n|\n");
			document.appendString("|  Version  r500\n");
			document.appendString("\\============================================\n");
			document.prompt();
		}
		catch(Exception e){}
    }
}
