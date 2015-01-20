/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 
 * SeparatorSplittingReader.java
 *
 * Created on 3. lokakuuta 2003, 9:03
 */

package org.wandora.utils;
import java.io.*;
import gnu.regexp.*;

/**
 * SeparatorSplittingReader splits files into multiple files according to
 * separator strings.The class extends PushbackReader. It's sole constructor
 * requires separator string to be given. Subsequent read()s return -1 not
 * only at the end of character stream, but also every time the separator
 * string is encountered in reading. To check whether the Reader is really at
 * the end of stream, the method eof() can be used. When a separator has
 * been encountered, reads return -1 until separator is skipped using method
 * skipSeparator().
 *
 * @author  pasi
 */
public class SeparatorSplittingReader extends java.io.PushbackReader {
    private java.util.Hashtable myseps = new java.util.Hashtable();
    private int pushbackbufsize = 1;
    private String atSeparator = null;
     /*
    public SeparatorSplittingReader(java.io.Reader in, int size, String separator) throws NullPointerException {
        super(in, size);
        if (separator.length() > size) {
            mysep = separator.substring(0, size);
        }
    }
      */
    public SeparatorSplittingReader(java.io.Reader in, int buflen) {
        super(in, buflen);
        pushbackbufsize = buflen;
    }
    
    public SeparatorSplittingReader(java.io.Reader in, String separator) throws NullPointerException {
        super(in, separator.length());
        pushbackbufsize = separator.length();
        myseps.put(separator, separator);
    }
    
    public void addSeparator(String separator) {
        int l = Math.min(pushbackbufsize, separator.length());
        myseps.put(separator, separator.substring(0, l));
    }
    
    public void removeSeparator(String separator) {
        myseps.remove(separator);
    }
    
    public boolean skipSeparator() throws java.io.IOException {
        if (atSeparator!=null) {
            super.skip(atSeparator.length());
            atSeparator = null;
            return true; // skipped ok
        } else return false; // was not at separator
    }
    
    // override read()
    public int read() throws IOException {
        if (atSeparator!=null) {
            return -1;
        }
        outer: for (java.util.Iterator itr = myseps.values().iterator(); itr.hasNext(); ) {
            String mysep = (String)itr.next();
            char [] pb_buf = new char[mysep.length()]; // pushback buffer internal to this method
            char [] pb; // actual pushback internal to this method
            for (int i = 0; i < mysep.length(); i++) {
                int c = super.read();
                if (c==-1) {
                    return -1;
                } else {
                    if (c!=mysep.charAt(i)) {
                        pb_buf[i] = (char)c;
                        super.unread(pb_buf, 0, i+1);
                        // ok, try next separator;
                        continue outer;
                    } else {
                        pb_buf[i] = (char)c;
                    }
                }
            }
            // match with current separator
            atSeparator = mysep;
            super.unread(pb_buf,0,mysep.length());
            return -1;
        }
        // none of the separators matched. return character.
        return super.read();
    }
    
    public void unread(char[] buf) throws java.io.IOException {
        atSeparator = null;
        super.unread(buf);
    }
    
    public void unread(char[] cbuf, int off, int len) throws java.io.IOException {
        atSeparator = null;
        super.unread(cbuf, off, len);
    }
    
    public void unread(int c) throws java.io.IOException {
        atSeparator = null;
        super.unread(c);
    }
    
    public boolean eof() {
        if (atSeparator!=null) return false;
        int c;
        try {
            c = super.read();
        } catch (Exception e) {
            return true; // this ok?
        }
        if (c==-1) return true;
        try {
            super.unread(c);
        } catch (Exception e) {
            return true; // this ok?
        }
        return false;
    }
    
    public String atSeparator() {
        return atSeparator;
    }
    
    /** Usage: $0  [-h header-separator] [-f footer-separator] [-g filename-regexp]
     * [-o output-filename] [-s separator-string] [-e charset] [-p output-path] file file ...
     *
     * Reads text input file, and splits it to separate files, using separator-string
     * as a divider. If no separator-string is given, default separator-string "\n" is used.
     *
     * If header-separator and/or footer-separator are specified, the
     * output files will all begin and/or end with header and/or footer from the
     * input file, defined by these separators. Also, splitting is not performed
     * before header and after footer, if they are specified. In other words,
     * each output file will have header from the input file, followed by a split
     * middle section from the input file, and finally followed by a footer from
     * the input file.
     *
     * If filename-regexp is given, each output file will be named with the regexp
     * match of filename-regexp applied to the current divider match by separator-regexp,
     * and will share their filename extension as the input file.
     *
     * If output-filename is given, it will be used as the output filename base, and
     * output filenames will be constructed by appending the base file name with
     * underscore and a sequential number starting with 0. Again, the output files will
     * share their filename extension with the input file.
     *
     * From filename-regexp and output-filename, filename-regexp takes precedence.
     * If neither is given, the base filename will be the base file name of the input
     * file, and underscore + sequence number are used.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String headerSeparatorString = null;
        String footerSeparatorString = null;
        boolean headerSeparatorFound = false;
        boolean footerSeparatorFound = false;
        String separatorString = "<foobar>";
        String outputFileNameBase = null;
        String outputPath = null;
        String outputFileNameRegexp = null;
        String charsetName = "UTF-8";
        java.util.Vector filesToProcess = new java.util.Vector();
        int i = 0;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase("-h") && i < args.length-1) {
                headerSeparatorString = args[++i];
                 System.out.println("Header separator string is = '"+headerSeparatorString+"'"); 
            } else if (args[i].equalsIgnoreCase("-f") && i < args.length-1) {
                footerSeparatorString = args[++i];
                System.out.println("Footer separator string is = '"+footerSeparatorString+"'"); 
            } else if (args[i].equalsIgnoreCase("-o") && i < args.length-1) {
                outputFileNameBase = args[++i];
                 System.out.println("Output filename base is = '"+outputFileNameBase+"'"); 
            } else if (args[i].equalsIgnoreCase("-s") && i < args.length-1) {
                separatorString = args[++i];
                System.out.println("Separator string is = '"+separatorString+"'"); 
            } else if (args[i].equalsIgnoreCase("-g") && i < args.length-1) {
                outputFileNameRegexp = args[++i];
                System.out.println("Output filename regexp is = '"+outputFileNameRegexp+"'"); 
            } else if (args[i].equalsIgnoreCase("-p") && i < args.length-1) {
                outputPath = args[++i];
                System.out.println("Output path is = '"+outputPath+"'"); 
            } else if (args[i].equalsIgnoreCase("-e") && i < args.length-1) {
                charsetName = args[++i];
                System.out.println("Charset is = '"+charsetName+"'"); 
            } else {
                filesToProcess.add(args[i]);
            }
            i++;
        }
        System.out.println("SeparatorSplittingReader.main(): args read.");
        for (java.util.Iterator itr = filesToProcess.iterator(); itr.hasNext(); ) {
            String inputFileName = (String) itr.next();
            System.out.println("SeparatorSplittingReader.main(): processing file '"+inputFileName+"'...");
            String extension = inputFileName.substring(inputFileName.lastIndexOf('.'));
            if (extension!=null && extension.length() > 0) {
                int lsepIndex = inputFileName.lastIndexOf(System.getProperty("file.separator"));
                int inputFileNameBaseStart = lsepIndex+1;
                outputFileNameBase = inputFileName.substring(inputFileNameBaseStart, inputFileName.lastIndexOf('.'));
            } else {
                extension = "";
            }
            java.io.Reader fr = null;
            try {
                fr  = new java.io.InputStreamReader(new java.io.FileInputStream(new java.io.File(inputFileName)), charsetName);
            } catch (java.io.FileNotFoundException fnfe) {
                System.err.println("SeparatorSplittingReader.main(): file '"+inputFileName+"' not found.");
                break;
            } catch (java.io.UnsupportedEncodingException usee) {
                System.err.println("SeparatorSplittingReader.main(): encoding '"+charsetName+"' not supported. Exiting.");
                System.exit(-1);
            }
            String separator;
            SeparatorSplittingReader rdr = null;
            StringBuffer headerBuffer = new StringBuffer();
            StringBuffer footerBuffer = new StringBuffer();
            StringBuffer contentBuffer;
            if (footerSeparatorString!=null) { // find footer first
                System.out.print(" scanning for footer separator '"+footerSeparatorString+"'...");
                // read in footer
                if (rdr==null) {
                    rdr = new SeparatorSplittingReader(fr, footerSeparatorString);
                }
                if (headerSeparatorString!=null) rdr.addSeparator(headerSeparatorString);
                // first find last occurrence of footerSeparatorString
                boolean found = false;
                int tcntr = 0;
                int c;
                char [] buffer = new char[1024]; // read header max 1 kb character at a time
                int cntr = 0;
                try {
                    while (!rdr.eof()) {
                        while(rdr.read()!=-1) {
                            tcntr++; // skip until stops
                            if (tcntr % (1024*1024) == 0) { System.out.print("."+tcntr); }
                        }
                        if (rdr.eof()) {
                            break;
                        }
                        if (headerSeparatorString!=null && headerSeparatorString.startsWith(rdr.atSeparator())) {
                            // header found!
                            System.out.print("(has header at "+ tcntr +")");
                            headerSeparatorFound = true;
                            rdr.skipSeparator();
                            continue;
                        }
                        if (footerSeparatorString != null && footerSeparatorString.startsWith(rdr.atSeparator())) {
                            System.out.print("(found footer at "+ tcntr +")");
                            rdr.skipSeparator();
                            footerSeparatorFound = true;
                            rdr.skipSeparator();
                            break;
                        }
                    }
                    while(!rdr.eof()) {
                        footerBuffer = new StringBuffer();
                        while((c=rdr.read())!=-1) {
                            tcntr++;
                            if (tcntr % (1024*1024) == 0) { System.out.print("."+tcntr); }
                            buffer[cntr++] = (char)c;
                            if (cntr>1023) {
                                footerBuffer.append(buffer, 0, cntr);
                                cntr = 0;
                            }
                        }
                    }
                    footerBuffer.append(buffer, 0,  cntr);
                    
                } catch (java.io.IOException ioe) {
                    System.err.println(args[0]+": reading file '"+inputFileName+"' caused i/o exception. Skipped.");
                    break;
                }
                
                try {
                    rdr.close();rdr=null;fr.close();fr=null;
                } catch (java.io.IOException ioe) {
                    System.err.println(args[0]+": closing file '"+inputFileName+"' caused i/o exception.");
                }
            }
            System.out.println("\n footer: '"+footerBuffer.toString()+"'...");
            // footerbuffer now ready. Process again using all three separators:
            try {
                fr  = new java.io.InputStreamReader(new java.io.FileInputStream(new java.io.File(inputFileName)), charsetName);
            } catch (java.io.FileNotFoundException fnfe) {
                System.err.println("SeparatorSplittingReader.main(): file '"+inputFileName+"' not found.");
                break;
            } catch (java.io.UnsupportedEncodingException usee) {
                System.err.println("SeparatorSplittingReader.main(): encoding '"+charsetName+"' not supported. Exiting.");
                System.exit(-1);
            }
            int fseplen, hseplen;
            if (footerSeparatorString!=null) fseplen = footerSeparatorString.length(); else fseplen = 0;
            if (headerSeparatorString!=null) hseplen = headerSeparatorString.length(); else hseplen = 0;
            int maxseplen = 1024; //Math.max(Math.max(fseplen, hseplen), separatorString.length());
            rdr =  new SeparatorSplittingReader(fr, maxseplen);
            if (footerSeparatorString!=null) rdr.addSeparator(footerSeparatorString);
            if (headerSeparatorString!=null) rdr.addSeparator(headerSeparatorString);
            rdr.addSeparator(separatorString);
            boolean processed = false;
            boolean headerBufferOk = false;
            int splitcntr=0;
            System.out.println("\n pass 2...");
            int tcntr = 0;
            while(!processed && !rdr.eof()) {
                StringBuffer splitBuffer = new StringBuffer();
                char [] buffer = new char[1024]; // read stuff max 1 kb character at a time
                int cntr = 0;
                int c;
                try {
                    header:
                        while(!rdr.eof() && headerSeparatorFound) {
                            while((c=rdr.read())!=-1) {
                                tcntr++;
                                if (tcntr % (1024*1024) == 0) { System.out.print("."+tcntr); }
                                buffer[cntr++] = (char)c;
                                if (cntr>1023) {
                                    splitBuffer.append(buffer, 0, cntr);
                                    cntr = 0;
                                }
                            }
                            if (headerSeparatorString.startsWith(rdr.atSeparator)) {
                                // header found!
                                splitBuffer.append(buffer,0,cntr);
                                cntr=0;
                                headerBuffer = splitBuffer;
                                splitBuffer = new StringBuffer();
                                System.out.println("\n Header extracted: '"+headerBuffer.toString()+"'.");                                
                                break header;
                            } else {
                                splitBuffer.append(rdr.atSeparator());
                            }
                            if (!rdr.eof()) rdr.skipSeparator();
                        }
                        System.out.print("\n scanning for '"+separatorString+"'...");    
                        while(!rdr.eof()) { // split loop
                            while((c=rdr.read())!=-1) {
                                tcntr++;
                                if (tcntr % (1024*1024) == 0) { System.out.print("."+tcntr); }
                                buffer[cntr++] = (char)c;
                                if (cntr>1023) {
                                    splitBuffer.append(buffer,0,cntr);
                                    cntr = 0;
                                }
                            }
                            
                            if (!rdr.eof()) { // not eof
                                if (rdr.atSeparator().equalsIgnoreCase(separatorString)) {
                                    System.out.println("found '"+separatorString+"'.");
                                    splitBuffer.append(buffer,0,cntr);
                                    cntr = 0;
                                    contentBuffer = splitBuffer;
                                    splitBuffer = new StringBuffer();
                                    // solve output file name:
                                    try { rdr.skipSeparator(); } catch (java.io.IOException ioe) { } // ignore for now
                                    String ofn;
                                    String path = "";
                                    if (outputPath!=null) path = outputPath;
                                    else {
                                        path = inputFileName.substring(0, inputFileName.lastIndexOf(System.getProperty("file.separator")));
                                    }
                                    if (outputFileNameRegexp != null) {
                                        char [] cbuf = new char[1024];
                                        int cread = 0;
                                        try {
                                            cread = rdr.read(cbuf, 0, cbuf.length);
                                            rdr.unread(cbuf, 0, cread);
                                        } catch (java.io.IOException ioe) { 
                                        
                                        } // ignore for now
                                        StringBuffer sectionStart = new StringBuffer();
                                        sectionStart.append(cbuf, 0, cread);
                                        ofn = matchString(outputFileNameRegexp, sectionStart.toString());
                                        if (ofn==null) {
                                            ofn = path + System.getProperty("file.separator")+outputFileNameBase + "_"+ splitcntr++ + extension;
                                        } else {
                                            System.out.println("Output file name base from regexp: '"+ofn+"'");
                                            ofn = path + System.getProperty("file.separator")+ ofn + extension;
                                        }
                                    } else {
                                        ofn = outputFileNameBase + "_"+ splitcntr++ + extension;
                                    }
                                    createDirectoriesIfNeeded(ofn);
                                    String nfn = preventOverWrite(ofn); // if a file with a name already exists, concat number until unique file name
                                    if (!nfn.equals(ofn)) {
                                        System.out.println("File "+ofn+" exists, using new file name "+nfn);
                                        ofn = nfn;
                                    }
                                    System.out.println("\n outputting file '"+ofn+"'...");
                                    // now output file:
                                    java.io.FileOutputStream fos = null;
                                    java.io.Writer wr = null;
                                    try {
                                        fos = new FileOutputStream(ofn);
                                        wr = new java.io.OutputStreamWriter(fos, charsetName);
                                        wr.write(headerBuffer.toString());
                                        wr.write(contentBuffer.toString());
                                        wr.write(footerBuffer.toString());
                                        contentBuffer = null;
                                    } catch (java.io.FileNotFoundException fnfe) {
                                        System.err.println(args[0]+": opening file '"+ofn+"' for writing caused i/o exception. skipping.");
                                    } catch (java.io.UnsupportedEncodingException usee) {
                                        System.err.println(args[0]+": encoding '"+ charsetName + "' is unsupported. Skipping writing for file'"+ofn+"'");
                                    } catch (java.io.IOException ioe) {
                                        System.err.println(args[0]+": writing to file '"+ofn+"' caused i/o exception. skipping.");
                                    } finally {
                                        try { wr.close(); } catch (Exception ee) { }
                                        try {fos.close(); } catch (Exception ee) {}
                                    }
                                } else if (rdr.atSeparator().equalsIgnoreCase(footerSeparatorString)) {
                                    System.out.println("Encountered footer separator '"+footerSeparatorString+"'- scanning over.");  
                                    processed = true;
                                    break;
                                } else {
                                    splitBuffer.append(rdr.atSeparator());
                                    rdr.skipSeparator();
                                }
                            } // if not eof
                        } // split loop
                } catch (java.io.IOException ioe) {
                    System.err.println(args[0]+": reading file '"+inputFileName+"' caused i/o exception. skipping.");
                    break;
                }
            } // processed
            try {
                rdr.close();
            } catch (java.io.IOException ioe) {
            }
            rdr = null;
        } // next input file
        System.out.println("\n all files processed.");
    }
    
    private static String matchString(String regularExpression, String string) {
        //System.out.println("matchString(\""+regularExpression+"\",  \""+string+"\");");
        try {
            RE re = new RE(regularExpression, RE.REG_ICASE, new RESyntax(RESyntax.RE_SYNTAX_PERL5));
            REMatch rm = re.getMatch(string); 
            if (rm!=null) 
                return rm.substituteInto("$1");
            else return null;
        } catch (Exception e) {
            System.out.println("Exception e:"+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void createDirectoriesIfNeeded(String filename) {
        //System.out.println("checking path:"+filename);
        int seppos = -1;
        String pathSep = System.getProperty("file.separator");
        while ((seppos=filename.indexOf(pathSep, seppos+1)) != -1) {
            String path = filename.substring(0,seppos);
            //System.out.println("checking dir:"+path);
            File f = new File(path);
            if (f.exists() && f.isDirectory()) continue;
            if (f.exists()) return;
            System.out.println("creating dir:"+path);
            f.mkdir();
        }
    }
    
    private static String preventOverWrite(String filename) {
        int cntr = 0;
        boolean unique = false;
        String filenamestr = filename;
        while (!unique) {
            File f = new File(filenamestr);
            if (f.exists()) {
                filenamestr = filename + "" + cntr++;
            } else {
                unique = true;
            }
        }
        return filenamestr;
    }
}