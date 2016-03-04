/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * RISReference.java
 *
 * Created on March 3, 2008, 1:49 PM
 */

package org.wandora.application.tools.extractors.ris;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wandora.application.WandoraToolLogger;
import static org.wandora.utils.Tuples.*;
/**
 *
 * @author anttirt
 */
public class RISReference {
    
    public String                           refID = null,
                                            titleTag = null,
                                            secondaryTitleTag = null,
                                            seriesTag = null,
                                            publicationCity = null,
                                            publisher = null,
                                            serial = null,
                                            address = null, // ISSN/ISBN
                                            availability = null,
                                            issue = null,
                                            startPage = null,
                                            endPage = null;
    public ArrayList<URL>                   mainURL = new ArrayList<URL>(),
                                            pdfURL = new ArrayList<URL>(),
                                            fullTextURL = new ArrayList<URL>();
    public Integer                          volumeNumber = null;
    public ReferenceType                    refType = null;
    public ArrayList<T2<Types, String>>     miscTags = new ArrayList<T2<Types, String>>(),
                                            unknownTags = new ArrayList<T2<Types, String>>(),
                                            authorTags = new ArrayList<T2<Types, String>>(),
                                            noteTags = new ArrayList<T2<Types, String>>(),
                                            userTags = new ArrayList<T2<Types, String>>();
    public Date                             dateTag = null;
    public ArrayList<T2<ReprintType, Date>> reprintInfo = null;
    public String                           periodicalName = null;
    public ArrayList<String>                periodicalAbbr = new ArrayList<String>(),
                                            keyWords = new ArrayList<String>(),
                                            relatedRecords = new ArrayList<String>();
    
    public DOI                              doi;
    
    private WandoraToolLogger mLog;
    
    public static String getWart(ReferenceType type)
    {
        String baseStr = "";
        switch(type)
        {
            case ABST: baseStr = "abstract"; break;
            case ADVS: baseStr = "audiovisual"; break;
            case ART: baseStr = "artwork"; break;
            case BILL: baseStr = "bill/resolution"; break;
            case BOOK: baseStr = "book"; break;
            case CASE: baseStr = "case"; break;
            case CHAP: baseStr = "chapter"; break;
            case COMP: baseStr = "computer program"; break;
            case CONF: baseStr = "conference proceeding"; break;
            case CTLG: baseStr = "catalog"; break;
            case DATA: baseStr = "data file"; break;
            case ELEC: baseStr = "electronic citation"; break;
            //case GEN: baseStr = "generic"; break;
            case HEAR: baseStr = "hearing"; break;
            case ICOMM: baseStr = "internet communication"; break;
            case INPR: baseStr = "in press"; break;
            case JFULL: baseStr = "journal"; break;
            case JOUR: baseStr = "article"; break;
            case MAP: baseStr = "map"; break;
            case MGZN: baseStr = "article"; break;
            case MPCT: baseStr = "motion picture"; break;
            case MUSIC: baseStr = "music score"; break;
            case NEWS: baseStr = "newspaper"; break;
            case PAMP: baseStr = "pamphlet"; break;
            case PAT: baseStr = "patent"; break;
            case PCOMM: baseStr = "personal communication"; break;
            case RPRT: baseStr = "report"; break;
            case SER: baseStr = "serial"; break;
            case SLIDE: baseStr = "slide"; break;
            case SOUND: baseStr = "recording"; break;
            case STAT: baseStr = "statute"; break;
            case THES: baseStr = "thesis"; break;
            case UNBILL: baseStr = "unenacted bill/resolution"; break;
            case UNPB: baseStr = "unpublished work"; break;
            case VIDEO: baseStr = "video recording"; break;
            default:
                baseStr = "title";
        }
        
        return " (" + baseStr + ")";
    }
    
    public static class DOI
    {
        public int DirectoryCode;
        public int PublisherID;
        public String Suffix;
        
        public DOI(int dirCode, int pubID, String suffix)
        {
            DirectoryCode = dirCode;
            PublisherID = pubID;
            Suffix = suffix;
        }
        
        public String toString()
        {
            return String.valueOf(DirectoryCode) + '.' + String.valueOf(PublisherID) + '/' + Suffix;
        }
        
        public static String getPublisherName(int publisherID)
        {
            switch(publisherID)
            {
                case 1002:
                    return "Wiley";
                case 1006:
                    return "Academic Press";
                case 1007:
                    return "Springer Verlag";
                case 1016:
                    return "Elsevier Science";
                case 1017:
                    return "Cambridge University Press";
                case 1038:
                    return "American Chemical Society";
                case 1039:
                    return "NATURE";
                case 1046:
                    return "Royal Society of Chemistry";
                case 1055:
                    return "Blackwell Publishers";
                case 1063:
                    return "G. Thieme Verlag";
                case 1073:
                    return "American Institute of Physics";
                case 1074:
                    return "Proceedings of the National Academy of Sciences";
                case 1080:
                    return "Journal of Biological Chemistry";
                case 1083:
                    return "Taylor and Francis";
                case 1092:
                    return "Laser Pages Publishing";
                case 1093:
                    return "Oxford University Press";
                case 1103:
                    return "American Physical Society";
                case 1107:
                    return "International Union of Crystallography";
                case 1126:
                    return "SCIENCE magazine";
                case 1161:
                    return "American Heart Association";
                case 1182:
                    return "American Society of hematology";
            }
            
            return null;
        }
    }
    

    
    public RISReference(CharBuffer buffer, WandoraToolLogger log) throws IllegalArgumentException {
        mLog = log;
        try
        {
            Pattern firstPattern = Pattern.compile("TY  - ");
            {
                CharSequence seq = buffer.subSequence(0, buffer.remaining());
                Matcher findFirst = firstPattern.matcher(seq);

                if(!findFirst.find())
                    throw new IllegalArgumentException("Unable to find start tag");
                buffer.position(buffer.position() + findFirst.start());
            }
            Types firstTag = Types.INVALID;
            /*
            try
            {
             */
                firstTag = readTag(buffer);
                /*
            }
            catch(IllegalArgumentException e)
            {
                // If the first tag read failed, readString to get any extra stuff and try again until we run out of characters
                readString(buffer);
                firstTag = readTag(buffer);
            }
            
            switch(firstTag)
            {
                case TY:
                    try
                    {
                        */
                        refType = ReferenceType.valueOf(readString(buffer));
                        /*
                    }
                    catch(IllegalArgumentException e)
                    {
                        if(log != null) log.log("Unrecognized reference category. " + e.toString());
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Invalid tag: " + firstTag.toString() + " (expected TY)");
            }
            */
            
            Pattern datePattern = Pattern.compile("(\\d+)");
        
            // this either returns on ER or throws on invalid data
            for(;;)
            {
                Types type = readTag(buffer);
                String contents = type == Types.ER ? ignoreCRLF(buffer) : readString(buffer);

                switch(type)
                {
                    // authors
                    case A1: case AU: case A2: case ED: case A3:
                        // TODO: Function for parsing names from format: LastName, FirstName[, Suffix]
                        authorTags.add(t2(type, contents));
                        break;

                    // primary title
                    case T1: case TI: case CT:
                        titleTag = contents;
                        break;
                       
                    // ???
                    case BT:
                        if(refType == ReferenceType.BOOK || refType == ReferenceType.UNPB)
                            titleTag = contents;
                        else
                            secondaryTitleTag = contents;
                        
                        break;
                       
                    // secondary title:
                    case T2:
                        secondaryTitleTag = contents;
                        break;
                        
                    // title series:
                    case T3:
                        seriesTag = contents;
                        break;
                        
                    // dates
                    case Y1: case PY: case Y2: {
                        Matcher m = datePattern.matcher(contents);
                        Integer year = null, month = null, day = null;
                        if(m.find(0))
                        {
                            year = Integer.parseInt(m.group());
                            if(m.find())
                            {
                                month = Integer.parseInt(m.group());
                                if(m.find())
                                {
                                    day = Integer.parseInt(m.group());
                                }
                            }
                        }
                        dateTag = new Date(year, month, day, "");
                        break;
                    }

                    // notes
                    case N1: case AB: case N2:
                        noteTags.add(t2(type, contents));
                        break;

                    // keywords
                    case KW:
                        keyWords.add(contents);
                        break;

                    // periodical name and abbr.
                    case JF: case JO:
                        periodicalName = contents;
                        break;
                        
                    case JA: case J1: case J2:
                        periodicalAbbr.add(contents);
                        break;
                        
                    // reference id
                    case ID:
                        refID = contents;
                        break;
                        
                    case VL: case IS: case CP:
                        issue = contents;
                        break;
                        
                    case SP:
                        startPage = contents;
                        break;
                        
                    case EP:
                        endPage = contents;
                        break;
                        
                    case CY:
                        publicationCity = contents;
                        break;
                        
                    case PB:
                        publisher = contents;
                        break;
                        
                    case SN:
                        serial = contents;
                        break;
                        
                    case AD:
                        address = contents;
                        break;
                        
                    case AV: // availability
                        availability = contents;
                        break;
                        
                    case UR: // url
                        try
                        {
                            mainURL.add(new URI(contents).toURL());
                        }
                        catch(URISyntaxException e) { if(log != null) log.log("Invalid URI syntax for web link: " + e.toString()); }
                        catch(MalformedURLException e) { if(log != null) log.log("Malformed URL for web link: " + e.toString()); }
                        break;
                        
                    case L1: // link(s) to pdf
                        try
                        {
                            pdfURL.add(new URI(contents).toURL());
                        }
                        catch(URISyntaxException e) { if(log != null) log.log("Invalid URI syntax for pdf link: " + e.toString()); }
                        catch(MalformedURLException e) { if(log != null) log.log("Malformed URL for pdf link: " + e.toString()); }
                        break;
                        
                    case L2: // link(s) to full-text
                        try
                        {
                            fullTextURL.add(new URI(contents).toURL());
                        }
                        catch(URISyntaxException e) { if(log != null) log.log("Invalid URI syntax for full-text link: " + e.toString()); }
                        catch(MalformedURLException e) { if(log != null) log.log("Malformed URL for full-text link: " + e.toString()); }
                        break;
                        
                    case L3: // related record(s)
                        break;
                        
                    case L4: // image(s)
                        break;
                        
                    case M1: case M2: case M3:
                        if(!tryInterpret(contents))
                            miscTags.add(t2(type, contents));
                        break;
                        
                    case U1: case U2: case U3: case U4: case U5:
                        userTags.add(t2(type, contents));
                        break;
                        
                    // done, discard any extra stuff that might be left (newlines etc non-tag data - will stop on first tag)
                    case ER:
                        {
                            CharSequence seq = buffer.subSequence(0, buffer.remaining());
                            Matcher findFirst = firstPattern.matcher(seq);

                            if(!findFirst.find())
                                buffer.position(buffer.position() + buffer.remaining());
                        }
                        return;

                    default:
                        unknownTags.add(t2(type, contents));
                        break;
                }
            }
        }
        catch(BufferUnderflowException e)
        {
            throw new IllegalArgumentException("Unable to find terminating tag (ER).");
        }
    }

    private Pattern ElsevierDOI = Pattern.compile("s(\\d{4}-\\d{4})\\((\\d{2})\\)\\d{5}-\\d");
    
    private void importDOI(DOI doi)
    {
        this.doi = doi;
        
        Matcher matcher;
        
        switch(doi.PublisherID)
        {
            case 1016: case 1107:
                matcher = ElsevierDOI.matcher(doi.Suffix);
                if(matcher.matches())
                {
                    serial = matcher.group(1); // ISSN
                    if(dateTag == null) dateTag = new Date(null, null, null, null);
                    if(dateTag.Year == null)
                    {
                        int year = Integer.decode(matcher.group(2));
                        if(year > 90)
                            year += 1900;
                        else
                            year += 2000;
                        
                        dateTag.Year = year;
                    }
                }
                break;
            default:
                break;
        }
    }
    
    private Pattern DOIPattern = Pattern.compile("(?:doi:)?(\\d\\d).(\\d\\d\\d\\d)/(.+)");
    
    private boolean tryInterpret(String data)
    {
        Matcher matcher;
        
        matcher = DOIPattern.matcher(data);
        if(matcher.matches())
        {
            importDOI(new DOI(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)), matcher.group(3)));
            return true;
        }
        
        return false;
    }
    
    private String ignoreCRLF(CharBuffer buffer)
    {
        String ret = "";
        try
        {
            char next = buffer.get();
            if(next == '\r' || next == '\n')
            {
                ret = null;
                if(next == '\r')
                {
                    next = buffer.get();
                    if(next != '\n')
                    {
                        buffer.position(buffer.position() - 1);
                    }
                }
            }
            else
            {
                buffer.position(buffer.position() - 1);
            }
        }
        catch(BufferUnderflowException e)
        {
        }
        
        return ret;
    }
    
    private String readString(CharBuffer buffer)
    {
        StringBuilder bldr = new StringBuilder();
        
        try
        {
            try
            {
                int pos = buffer.position();
                readTag(buffer);
                buffer.position(pos);
                return "";
            }
            catch(IllegalArgumentException e)
            {
                
            }
            for(;;)
            {
                char next = buffer.get();
                if(next == '\r' || next == '\n') // newline?
                {
                    if(next == '\r') // win / mac, else posix
                    {
                        next = buffer.get();
                        if(next != '\n') // mac, else win
                        {
                            buffer.position(buffer.position() - 1);
                        }
                    }
                    
                    try
                    {
                        // if the next line starts with a tag, we're done
                        int pos = buffer.position();
                        readTag(buffer);
                        buffer.position(pos);

                        return bldr.toString();
                    }
                    catch(IllegalArgumentException e)
                    {
                        // otherwise continue, replacing newline with space
                        bldr.append(' ');
                    }
                }
                else
                {
                    bldr.append(next);
                }
            }
        }
        catch(BufferUnderflowException e)
        {
            return bldr.toString();
        }
    }
    
    private Types readTag(CharBuffer buffer) throws IllegalArgumentException, BufferUnderflowException
    {
        while(ignoreCRLF(buffer) == null);
        
        char[] readBuf = new char[6];
        readBuf[0] = buffer.get();
        readBuf[1] = buffer.get();
        readBuf[2] = buffer.get();
        readBuf[3] = buffer.get();
        readBuf[4] = buffer.get();

        String str = String.valueOf(readBuf, 0, 5);
        
        if(str.equals("ER  -"))
        {
            try
            {
                readBuf[5] = buffer.get();
                if(readBuf[5] != ' ')
                    buffer.position(buffer.position() - 1);
            }
            catch(BufferUnderflowException e) { }
            
            return Types.ER;
        }
        
        readBuf[5] = buffer.get();
        
        if(!"  - ".equals(String.valueOf(readBuf, 2, 4)))
        {
            buffer.position(buffer.position() - 6); // rewind to before read
            throw new IllegalArgumentException("Invalid format: " + String.valueOf(readBuf) + " (expected \"TY  - \"");
        }
        
        return Types.valueOf(String.valueOf(readBuf, 0, 2));
    }
    
    public static class Date
    {
        public Integer Year;
        public Integer Month;
        public Integer Day;
        public String Info;
        
        public Date(Integer year, Integer month, Integer day, String info)
        {
            Year = year;
            Month = month;
            Day = day;
            Info = info;
        }
    }
    
    public static class JournalInfo
    {
        public String Issue;
        public Integer StartPage, EndPage;
        
        public JournalInfo(String issue, Integer startPage, Integer endPage)
        {
            Issue = issue;
            StartPage = startPage;
            EndPage = endPage;
        }
    }
    
    public static enum ReprintType
    {
        ON_REQUEST,
        NOT_IN_FILE,
        IN_FILE,
    }
    
    public static enum ReferenceType
    {
        ABST,
        ADVS,
        ART,
        BILL,
        BOOK,
        CASE,
        CHAP,
        CHAPTER,
        COMP,
        CONF,
        CTLG,
        DATA,
        ELEC,
        GEN,
        HEAR,
        ICOMM,
        INPR,
        JFULL,
        JOUR,
        MAP,
        MGZN,
        MPCT,
        MUSIC,
        NEWS,
        PAMP,
        PAT,
        PCOMM,
        RPRT,
        SER,
        SLIDE,
        SOUND,
        STAT,
        THES,
        UNBILL,
        UNPB,
        VIDEO,
    }
    
    public static enum Types
    {
        // Title and Reference Type Tags
        TY,
        ER,
        ID,
        T1,
        TI,
        CT,
        BT,
        T2,
        T3,

        // Authors
        A1,
        AU,
        A2,
        ED,
        A3,

        // Year and Free Text Fields
        Y1,
        PY,
        Y2,
        N1,
        AB,
        N2,

        // Keywords and Reprint Status
        KW,
        RP,

        // Periodical Tags
        JF,
        JO,
        JA,
        J1,
        J2,

        // Periodical and Publisher Tags
        VL,
        IS,
        CP,
        SP,
        EP,
        CY,
        PB,
        SN,
        AD,

        // Misc. Tags
        AV,
        M1,
        M2,
        M3,
        U1,
        U2,
        U3,
        U4,
        U5,
        UR,
        L1,
        L2,
        L3,
        L4,
        
        INVALID,
    }
}