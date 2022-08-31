package org.apache.maven.jxr;

/*
 * CodeViewer.java
 * CoolServlets.com
 * March 2000
 *
 * Copyright (C) 2000 CoolServlets.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1) Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 3) Neither the name CoolServlets.com nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY COOLSERVLETS.COM AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL COOLSERVLETS.COM OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.maven.jxr.pacman.ClassType;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.ImportType;
import org.apache.maven.jxr.pacman.JavaFile;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.jxr.pacman.PackageType;
import org.apache.maven.jxr.util.SimpleWordTokenizer;
import org.apache.maven.jxr.util.StringEntry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Syntax highlights java by turning it into html. A codeviewer object is created and then keeps state as lines are
 * passed in. Each line passed in as java test, is returned as syntax highlighted html text. Users of the class can set
 * how the java code will be highlighted with setter methods. Only valid java lines should be passed in since the object
 * maintains state and may not handle illegal code gracefully. The actual system is implemented as a series of filters
 * that deal with specific portions of the java code. The filters are as follows:
 *
 * <pre>
 *  htmlFilter
 *    |__
 *      ongoingMultiLineCommentFilter -&gt; uriFilter
 *        |__
 *          inlineCommentFilter
 *            |__
 *              beginMultiLineCommentFilter -&gt; ongoingMultiLineCommentFilter
 *                |__
 *                  stringFilter
 *                    |__
 *                      keywordFilter
 *                        |__
 *                          uriFilter
 *                            |__
 *                              jxrFilter
 *                                |__
 *                                  importFilter
 * </pre>
 */
public class JavaCodeTransform
    implements Serializable
{
    // ----------------------------------------------------------------------
    // public fields
    // ----------------------------------------------------------------------

    /**
     * show line numbers
     */
    private static final boolean LINE_NUMBERS = true;

    /**
     * start comment delimiter
     */
    private static final String COMMENT_START = "<em class=\"jxr_comment\">";

    /**
     * end comment delimiter
     */
    private static final String COMMENT_END = "</em>";

    /**
     * start javadoc comment delimiter
     */
    private static final String JAVADOC_COMMENT_START = "<em class=\"jxr_javadoccomment\">";

    /**
     * end javadoc comment delimiter
     */
    private static final String JAVADOC_COMMENT_END = "</em>";

    /**
     * start String delimiter
     */
    private static final String STRING_START = "<span class=\"jxr_string\">";

    /**
     * end String delimiter
     */
    private static final String STRING_END = "</span>";

    /**
     * start reserved word delimiter
     */
    private static final String RESERVED_WORD_START = "<strong class=\"jxr_keyword\">";

    /**
     * end reserved word delimiter
     */
    private static final String RESERVED_WORD_END = "</strong>";

    /**
     * stylesheet file name
     */
    private static final String STYLESHEET_FILENAME = "stylesheet.css";

    private static final String[] VALID_URI_SCHEMES = { "http://", "https://", "mailto:" };

    /**
     * Specify the only characters that are allowed in a URI besides alpha and numeric characters. Refer RFC2396 -
     * http://www.ietf.org/rfc/rfc2396.txt
     */
    private static final char[] VALID_URI_CHARS = { '?', '+', '%', '&', ':', '/', '.', '@', '_', ';', '=', '$', ',',
        '-', '!', '~', '*', '\'', '(', ')' };

    // ----------------------------------------------------------------------
    // private fields
    // ----------------------------------------------------------------------

    /**
     * HashTable containing java reserved words
     */
    private Map<String, String> reservedWords = new Hashtable<>();

    /**
     * Flag set to true when a multi-line comment is started.
     */
    private boolean inMultiLineComment = false;

    /**
     * Flag set to true when a javadoc comment is started.
     */
    private boolean inJavadocComment = false;

    /**
     * File name that is currently being processed.
     */
    private Path currentFilename = null;

    /**
     * Revision of the currently transformed document.
     */
    private String revision = null;

    /**
     * The output encoding
     */
    private String outputEncoding = null;

    /**
     * The wanted locale
     */
    private Locale locale = null;

    /**
     * Relative path to javadocs, suitable for hyperlinking.
     */
    private Path javadocLinkDir;

    /**
     * Package Manager for this project.
     */
    private final PackageManager packageManager;

    /**
     * current file manager
     */
    private final FileManager fileManager;

    {
        reservedWords.put( "abstract", "abstract" );
        reservedWords.put( "do", "do" );
        reservedWords.put( "inner", "inner" );
        reservedWords.put( "public", "public" );
        reservedWords.put( "var", "var" );
        reservedWords.put( "boolean", "boolean" );
        reservedWords.put( "continue", "continue" );
        reservedWords.put( "int", "int" );
        reservedWords.put( "return", "return" );
        reservedWords.put( "void", "void" );
        reservedWords.put( "break", "break" );
        reservedWords.put( "else", "else" );
        reservedWords.put( "interface", "interface" );
        reservedWords.put( "short", "short" );
        reservedWords.put( "volatile", "volatile" );
        reservedWords.put( "byvalue", "byvalue" );
        reservedWords.put( "extends", "extends" );
        reservedWords.put( "long", "long" );
        reservedWords.put( "static", "static" );
        reservedWords.put( "while", "while" );
        reservedWords.put( "case", "case" );
        reservedWords.put( "final", "final" );
        reservedWords.put( "native", "native" );
        reservedWords.put( "super", "super" );
        reservedWords.put( "transient", "transient" );
        reservedWords.put( "cast", "cast" );
        reservedWords.put( "float", "float" );
        reservedWords.put( "new", "new" );
        reservedWords.put( "rest", "rest" );
        reservedWords.put( "catch", "catch" );
        reservedWords.put( "for", "for" );
        reservedWords.put( "null", "null" );
        reservedWords.put( "synchronized", "synchronized" );
        reservedWords.put( "char", "char" );
        reservedWords.put( "finally", "finally" );
        reservedWords.put( "operator", "operator" );
        reservedWords.put( "this", "this" );
        reservedWords.put( "class", "class" );
        reservedWords.put( "generic", "generic" );
        reservedWords.put( "outer", "outer" );
        reservedWords.put( "switch", "switch" );
        reservedWords.put( "const", "const" );
        reservedWords.put( "goto", "goto" );
        reservedWords.put( "package", "package" );
        reservedWords.put( "throw", "throw" );
        reservedWords.put( "double", "double" );
        reservedWords.put( "if", "if" );
        reservedWords.put( "private", "private" );
        reservedWords.put( "true", "true" );
        reservedWords.put( "default", "default" );
        reservedWords.put( "import", "import" );
        reservedWords.put( "protected", "protected" );
        reservedWords.put( "try", "try" );
        reservedWords.put( "throws", "throws" );
        reservedWords.put( "implements", "implements" );
    }

    public JavaCodeTransform( PackageManager packageManager, FileManager fileManager )
    {
        this.packageManager = packageManager;
        this.fileManager = fileManager;
    }

    // ----------------------------------------------------------------------
    // public methods
    // ----------------------------------------------------------------------

    /**
     * Now different method of seeing if at end of input stream, closes inputs stream at end.
     *
     * @param line String
     * @return filtered line of code
     */
    private String syntaxHighlight( String line )
    {
        return htmlFilter( line );
    }

    /**
     * Gets the header attribute of the JavaCodeTransform object
     *
     * @param out the writer where the header is appended to
     */
    private void appendHeader( PrintWriter out )
    {
        String outputEncoding = this.outputEncoding;
        if ( outputEncoding == null )
        {
            outputEncoding = "ISO-8859-1";
        }

        // header
        out.println( "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" );
        out.print( "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"" );
        out.print( locale );
        out.print( "\" lang=\"" );
        out.print( locale );
        out.println( "\">" );
        out.print( "<head>" );
        out.print( "<meta http-equiv=\"content-type\" content=\"text/html; charset=" );
        out.print( outputEncoding );
        out.println( "\" />" );

        // title ("classname xref")
        out.print( "<title>" );
        try
        {
            JavaFile javaFile = fileManager.getFile( this.getCurrentFilename() );
            // Use the name of the file instead of the class to handle inner classes properly
            if ( javaFile.getClassType() != null && javaFile.getClassType().getFilename() != null )
            {
                out.print( javaFile.getClassType().getFilename() );
            }
            else
            {
                out.print( javaFile.getFilename() );
            }
            out.print( ' ' );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            out.println( "xref</title>" );
        }

        // stylesheet link
        out.print( "<link type=\"text/css\" rel=\"stylesheet\" href=\"" );
        out.print( this.getPackageRoot() );
        out.print( STYLESHEET_FILENAME );
        out.println( "\" />" );

        out.println( "</head>" );
        out.println( "<body>" );
        out.print( this.getFileOverview() );

        // start code section
        out.println( "<pre>" );
    }

    /**
     * Gets the footer attribute of the JavaCodeTransform object
     *
     * @param out the writer where the header is appended to
     * @param bottom the bottom text
     */
    private void appendFooter( PrintWriter out, String bottom )
    {
        out.println( "</pre>" );
        out.println( "<hr/>" );
        out.print( "<div id=\"footer\">" );
        out.print( bottom );
        out.println( "</div>" );
        out.println( "</body>" );
        out.println( "</html>" );
    }

    /**
     * This is the public method for doing all transforms of code.
     *
     * @param sourceReader Reader
     * @param destWriter Writer
     * @param locale String
     * @param outputEncoding String
     * @param javadocLinkDir String
     * @param revision String
     * @param bottom string
     * @throws IOException
     */
    private void transform( Reader sourceReader, Writer destWriter, Locale locale,
                                 String outputEncoding, Path javadocLinkDir, String revision, String bottom )
        throws IOException
    {
        this.locale = locale;
        this.outputEncoding = outputEncoding;
        this.javadocLinkDir = javadocLinkDir;
        this.revision = revision;

        BufferedReader in = new BufferedReader( sourceReader );

        PrintWriter out = new PrintWriter( destWriter );

        String line;

        appendHeader( out );

        int linenumber = 1;
        while ( ( line = in.readLine() ) != null )
        {
            if ( LINE_NUMBERS )
            {
                out.print( "<a class=\"jxr_linenumber\" name=\"L" + linenumber + "\" " + "href=\"#L" + linenumber
                    + "\">" + linenumber + "</a>" + getLineWidth( linenumber ) );
            }

            out.println( this.syntaxHighlight( line ) );

            ++linenumber;
        }

        appendFooter( out, bottom );

        out.flush();
    }

    /**
     * This is the public method for doing all transforms of code.
     *
     * @param sourcefile source file
     * @param destfile destination file
     * @param locale locale
     * @param inputEncoding input encoding
     * @param outputEncoding output encoding
     * @param javadocLinkDir relative path to javadocs
     * @param revision revision of the module
     * @param bottom bottom text
     * @throws IOException in I/O failures in reading/writing files
     */
    public final void transform( Path sourcefile, Path destfile, Locale locale, String inputEncoding,
                                 String outputEncoding, Path javadocLinkDir, String revision, String bottom )
        throws IOException
    {
        this.setCurrentFilename( sourcefile );

        // make sure that the parent directories exist...
        Files.createDirectories( destfile.getParent() );

        try ( Reader fr = getReader( sourcefile, inputEncoding ); Writer fw = getWriter( destfile, outputEncoding ) )
        {
            transform( fr, fw, locale, outputEncoding, javadocLinkDir, revision, bottom );
        }
        catch ( RuntimeException e )
        {
            System.out.println( "Unable to processPath " + sourcefile + " => " + destfile );
            throw e;
        }
    }

    private Writer getWriter( Path destfile, String outputEncoding )
        throws IOException
    {
        Writer fw;
        if ( outputEncoding != null )
        {
            fw = new OutputStreamWriter( new FileOutputStream( destfile.toFile() ), outputEncoding );
        }
        else
        {
            fw = new FileWriter( destfile.toFile() );
        }
        return fw;
    }

    private Reader getReader( Path sourcefile, String inputEncoding )
        throws IOException
    {
        Reader fr;
        if ( inputEncoding != null )
        {
            fr = new InputStreamReader( new FileInputStream( sourcefile.toFile() ), inputEncoding );
        }
        else
        {
            fr = new FileReader( sourcefile.toFile() );
        }
        return fr;
    }

    /**
     * Gets the current file name.
     *
     * @return path of file
     */
    private Path getCurrentFilename()
    {
        return this.currentFilename;
    }

    /**
     * Sets the current file name.
     *
     * @param filename file name
     */
    private void setCurrentFilename( Path filename )
    {
        this.currentFilename = filename;
    }

    /**
     * From the current file, determine the package root based on the current path.
     *
     * @return package root
     */
    private String getPackageRoot()
    {
        StringBuilder buff = new StringBuilder();

        JavaFile jf;

        try
        {
            jf = fileManager.getFile( this.getCurrentFilename() );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return null;
        }

        String current = jf.getPackageType().getName();

        int count = this.getPackageCount( current );

        for ( int i = 0; i < count; ++i )
        {
            buff.append( "../" );
        }

        return buff.toString();
    }

    /**
     * Given a line of text, search for URIs and make href's out of them.
     *
     * @param line String
     * @return href
     */
    private String uriFilter( String line )
    {
        for ( String scheme : VALID_URI_SCHEMES )
        {
            int index = line.indexOf( scheme );

            if ( index != -1 )
            {
                int start = index;
                int end = -1;

                for ( int j = start; j < line.length(); ++j )
                {
                    char current = line.charAt( j );

                    if ( !Character.isLetterOrDigit( current ) && isInvalidURICharacter( current ) )
                    {
                        end = j;
                        break;
                    }

                    end = j;
                }

                // now you should have the full URI so you can replace this
                // in the current buffer

                if ( end != -1 )
                {
                    String uri = ( end + 1 == line.length() ) ? line.substring( start ) : line.substring( start, end );

                    line = line.replace( uri, "<a href=\"" + uri + "\" target=\"alexandria_uri\">" + uri + "</a>" );
                }
            }
        }

        // if we are in a multiline comment we should not call JXR here.
        if ( !inMultiLineComment && !inJavadocComment )
        {
            return jxrFilter( line );
        }

        return line;
    }

    /**
     * The current revision of the module.
     *
     * @return revision
     */
    public final String getRevision()
    {
        return this.revision;
    }

    /**
     * Cross Reference the given line with JXR returning the new content.
     *
     * @param line line
     * @param packageName String
     * @param classType ClassType
     * @return cross-referenced line
     */
    private String xrLine( String line, String packageName, ClassType classType )
    {
        StringBuilder buff = new StringBuilder( line );

        String link;
        String find;
        String href;

        if ( classType != null )
        {
            href = this.getHREF( packageName, classType );
            find = classType.getName();

            // build out what the link would be.
            link = "<a name=\"" + find + "\" href=\"" + href + "\">" + find + "</a>";
        }
        else
        {
            href = this.getHREF( packageName );
            find = packageName;

            // build out what the link would be.
            link = "<a href=\"" + href + "\">" + find + "</a>";
        }

        // use the SimpleWordTokenizer to find all entries
        // that match word. Then replace these with the link

        // now replace the word in the buffer with the link

        String replace = link;
        List<StringEntry> tokens = SimpleWordTokenizer.tokenize( buff.toString(), find );

        // JXR-141: If there are more than 1 tokens to be replaced,
        // then the start+end values are out of order during the
        // buff.replace.
        // Reversing the list solves it
        Collections.reverse( tokens );

        for ( StringEntry token : tokens )
        {
            int start = token.getIndex();
            int end = token.getIndex() + find.length();

            buff.replace( start, end, replace );

        }

        return buff.toString();
    }

    // ----------------------------------------------------------------------
    // private methods
    // ----------------------------------------------------------------------

    /**
     * Filter html tags into more benign text.
     *
     * @param line String
     * @return html encoded line
     */
    private String htmlFilter( String line )
    {
        if ( line == null || line.equals( "" ) )
        {
            return "";
        }
        line = line.replace( "&", "&amp;" )
                    .replace( "<", "&lt;" )
                    .replace( ">", "&gt;" )
                    .replace( "\\\\", "&#92;&#92;" )
                    .replace( "\\\"", "\\&quot;" )
                    .replace( "'\"'", "'&quot;'" );
        return ongoingMultiLineCommentFilter( line );
    }

    /**
     * Handle ongoing multi-line comments, detecting ends if present.<br>
     * State is maintained in private boolean members,
     * one each for javadoc and (normal) multi-line comments.
     *
     * @param line line
     * @return processed line
     */
    private String ongoingMultiLineCommentFilter( String line )
    {
        if ( line == null || line.equals( "" ) )
        {
            return "";
        }
        final String[] tags =
            inJavadocComment ? new String[] { JAVADOC_COMMENT_START, JAVADOC_COMMENT_END }
                            : inMultiLineComment ? new String[] { COMMENT_START, COMMENT_END } : null;

        if ( tags == null )
        {
            // pass the line down to the next filter for processing.
            return inlineCommentFilter( line );
        }

        int index = line.indexOf( "*/" );
        // only filter the portion without the end-of-comment,
        // since * and / seem to be valid URI characters
        String comment = uriFilter( index < 0 ? line : line.substring( 0, index ) );
        if ( index >= 0 )
        {
            inJavadocComment = false;
            inMultiLineComment = false;
        }
        StringBuilder buf = new StringBuilder( tags[0] ).append( comment );

        if ( index >= 0 )
        {
            buf.append( "*/" );
        }
        buf.append( tags[1] );

        if ( index >= 0 && line.length() > index + 2 )
        {
            buf.append( inlineCommentFilter( line.substring( index + 2 ) ) );
        }
        return buf.toString();
    }

    /**
     * Filter inline comments from a line and formats them properly. One problem we'll have to solve here: comments
     * contained in a string should be ignored... this is also true of the multi-line comments. So, we could either
     * ignore the problem, or implement a function called something like isInsideString(line, index) where index points
     * to some point in the line that we need to check... started doing this function below.
     *
     * @param line line
     * @return processed line
     */
    private String inlineCommentFilter( String line )
    {
        // assert !inJavadocComment;
        // assert !inMultiLineComment;

        if ( line == null || line.equals( "" ) )
        {
            return "";
        }
        int index = line.indexOf( "//" );
        if ( ( index >= 0 ) && !isInsideString( line, index ) )
        {
            return beginMultiLineCommentFilter( line.substring( 0, index ) ) + COMMENT_START + line.substring( index )
                    + COMMENT_END;
        }

        return beginMultiLineCommentFilter( line );
    }

    /**
     * Detect and handle the start of multiLine comments. State is maintained in private boolean members one each for
     * javadoc and (normal) multiline comments.
     *
     * @param line line
     * @return processed line
     */
    private String beginMultiLineCommentFilter( String line )
    {
        // assert !inJavadocComment;
        // assert !inMultiLineComment;

        if ( line == null || line.equals( "" ) )
        {
            return "";
        }

        int index = line.indexOf( "/*" );
        // check to see if a multi-line comment starts on this line:
        if ( ( index > -1 ) && !isInsideString( line, index ) )
        {
            String fromIndex = line.substring( index );
            if ( fromIndex.startsWith( "/**" ) && !( fromIndex.startsWith( "/**/" ) ) )
            {
                inJavadocComment = true;
            }
            else
            {
                inMultiLineComment = true;
            }
            // Return result of other filters + everything after the start
            // of the multiline comment. We need to pass the through the
            // to the ongoing multiLineComment filter again in case the comment
            // ends on the same line.
            return stringFilter( line.substring( 0, index ) ) + ongoingMultiLineCommentFilter( fromIndex );
        }

        // Otherwise, no useful multi-line comment information was found so
        // pass the line down to the next filter for processesing.
        else
        {
            return stringFilter( line );
        }
    }

    /**
     * Filters strings from a line of text and formats them properly.
     *
     * @param line line
     * @return processed line
     */
    private String stringFilter( String line )
    {
        if ( line == null || line.equals( "" ) )
        {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        if ( line.indexOf( '"' ) <= -1 )
        {
            return keywordFilter( line );
        }
        int start = 0;
        int startStringIndex = -1;
        int endStringIndex = -1;
        int tempIndex;
        // Keep moving through String characters until we want to stop...
        while ( ( tempIndex = line.indexOf( '"' ) ) > -1 )
        {
            // We found the beginning of a string
            if ( startStringIndex == -1 )
            {
                startStringIndex = 0;
                buf.append( stringFilter( line.substring( start, tempIndex ) ) );
                buf.append( STRING_START ).append( '"' );
                line = line.substring( tempIndex + 1 );
            }
            // Must be at the end
            else
            {
                startStringIndex = -1;
                endStringIndex = tempIndex;
                buf.append( line, 0, endStringIndex + 1 );
                buf.append( STRING_END );
                line = line.substring( endStringIndex + 1 );
            }
        }

        buf.append( keywordFilter( line ) );

        return buf.toString();
    }

    /**
     * Filters keywords from a line of text and formats them properly.
     *
     * @param line line
     * @return processed line
     */
    private String keywordFilter( String line )
    {
        final String classKeyword = "class";

        if ( line == null || line.equals( "" ) )
        {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        int i = 0;
        char ch;
        StringBuilder temp = new StringBuilder();
        while ( i < line.length() )
        {
            temp.setLength( 0 );
            ch = line.charAt( i );
            while ( i < line.length() && ( ( ch >= 'a' && ch <= 'z' ) || ( ch >= 'A' && ch <= 'Z' ) ) )
            {
                temp.append( ch );
                i++;
                if ( i < line.length() )
                {
                    ch = line.charAt( i );
                }
            }
            String tempString = temp.toString();

            // Special handling of css style class definitions
            if ( classKeyword.equals( tempString ) && ch == '=' )
            {
                i++;
            }
            else if ( reservedWords.containsKey( tempString ) )
            {
                line = line.substring( 0, i - tempString.length() ) + RESERVED_WORD_START
                        + tempString
                        + RESERVED_WORD_END
                        + line.substring( i );
                i += ( RESERVED_WORD_START.length() + RESERVED_WORD_END.length() );
            }
            else
            {
                i++;
            }
        }
        buf.append( line );

        return uriFilter( buf.toString() );
    }

    /**
     * Checks to see if some position in a line is between String start and ending characters. Not yet used in code or
     * fully working :)
     *
     * @param line String
     * @param position int
     * @return boolean
     */
    private boolean isInsideString( String line, int position )
    {
        if ( line.indexOf( '"' ) < 0 )
        {
            return false;
        }
        int index;
        String left = line.substring( 0, position );
        String right = line.substring( position );
        int leftCount = 0;
        int rightCount = 0;
        while ( ( index = left.indexOf( '"' ) ) > -1 )
        {
            leftCount++;
            left = left.substring( index + 1 );
        }
        while ( ( index = right.indexOf( '"' ) ) > -1 )
        {
            rightCount++;
            right = right.substring( index + 1 );
        }
        return ( rightCount % 2 != 0 && leftCount % 2 != 0 );
    }

    /**
     * @param oos ObjectOutputStream
     * @throws IOException on I/O error during write
     */
    final void writeObject( ObjectOutputStream oos )
        throws IOException
    {
        oos.defaultWriteObject();
    }

    /**
     * @param ois object input stream
     * @throws ClassNotFoundException if the class of a serialized object could not be found.
     * @throws IOException on I/O error during read
     */
    final void readObject( ObjectInputStream ois )
        throws ClassNotFoundException, IOException
    {
        ois.defaultReadObject();
    }

    /**
     * Gets an overview header for this file.
     *
     * @return overview header
     */
    private String getFileOverview()
    {
        StringBuilder overview = new StringBuilder();

        // only add the header if javadocs are present
        if ( javadocLinkDir != null )
        {
            overview.append( "<div id=\"overview\">" );
            // get the URI to get Javadoc info.
            Path javadocURI;

            try
            {
                JavaFile jf = fileManager.getFile( this.getCurrentFilename() );

                javadocURI = javadocLinkDir.resolve( jf.getPackageType().getName().replace( '.', '/' ) )
                                ;
                // Use the name of the file instead of the class to handle inner classes properly
                String fileName;
                if ( jf.getClassType() != null && jf.getClassType().getFilename() != null )
                {
                    fileName = jf.getClassType().getFilename();
                }
                else
                {
                    fileName = jf.getFilename();
                }
                javadocURI = javadocURI.resolve( fileName + ".html" );

                String javadocHREF = "<a href=\"" + javadocURI.toString().replace( '\\', '/' ) + "\">View Javadoc</a>";

                // get the generation time...
                overview.append( javadocHREF );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }

            overview.append( "</div>" );
        }

        return overview.toString();
    }

    /**
     * Handles line width which may need to change depending on which line number you are on.
     *
     * @param linenumber int
     * @return blanks
     */
    private String getLineWidth( int linenumber )
    {
        if ( linenumber < 10 )
        {
            return "   ";
        }
        else if ( linenumber < 100 )
        {
            return "  ";
        }
        else
        {
            return " ";
        }
    }

    /**
     * Handles finding classes based on the current filename and then makes HREFs for you to link to them with.
     *
     * @param line line
     * @return processed line
     */
    private String jxrFilter( String line )
    {
        JavaFile jf;

        try
        {
            // if the current file isn't set then just return
            if ( this.getCurrentFilename() == null )
            {
                return line;
            }

            jf = fileManager.getFile( this.getCurrentFilename() );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return line;
        }

        Set<String> packages = new HashSet<>();

        // get the imported packages
        for ( ImportType importType : jf.getImportTypes() )
        {
            packages.add( importType.getPackage() );
        }

        // add the current package.
        packages.add( jf.getPackageType().getName() );

        List<StringEntry> words = SimpleWordTokenizer.tokenize( line );

        // go through each word and then match them to the correct class if necessary.
        for ( StringEntry word : words )
        {
            for ( String pkg : packages )
            {
                // get the package from the PackageManager because this will hold
                // the version with the classes also.

                PackageType currentImport = packageManager.getPackageType( pkg );

                // the package here might in fact be null because it wasn't parsed out
                // this might be something that is either not included or is part
                // of another package and wasn't parsed out.

                if ( currentImport == null )
                {
                    continue;
                }

                // see if the current word is within the package

                // at this point the word could be a fully qualified package name
                // (FQPN) or an imported package name.

                String wordName = word.toString();

                if ( wordName.indexOf( '.' ) != -1 )
                {
                    // if there is a "." in the string then we have to assume
                    // it is a package.

                    String fqpnPackage = wordName.substring( 0, wordName.lastIndexOf( '.' ) );
                    String fqpnClass = wordName.substring( wordName.lastIndexOf( '.' ) + 1 );

                    // note. since this is a reference to a full package then
                    // it doesn't have to be explicitly imported so this information
                    // is useless. Instead just see if it was parsed out.

                    PackageType pt = packageManager.getPackageType( fqpnPackage );

                    if ( pt != null )
                    {
                        ClassType ct = pt.getClassType( fqpnClass );

                        if ( ct != null )
                        {
                            // OK. the user specified a full package to be imported
                            // that is in the package manager so it is time to
                            // link to it.

                            line = xrLine( line, pt.getName(), ct );
                        }
                    }

                    if ( fqpnPackage.equals( currentImport.getName() )
                        && currentImport.getClassType( fqpnClass ) != null )
                    {
                        // then the package we are currently in is the one specified in the string
                        // and the import class is correct.
                        line = xrLine( line, pkg, currentImport.getClassType( fqpnClass ) );
                    }
                }
                else if ( currentImport.getClassType( wordName ) != null )
                {
                    line = xrLine( line, pkg, currentImport.getClassType( wordName ) );
                }
            }
        }

        return importFilter( line );
    }

    /**
     * Given the current package, get an HREF to the package and class given
     *
     * @param dest destination
     * @param jc class type
     * @return href
     */
    private String getHREF( String dest, ClassType jc )
    {
        StringBuilder href = new StringBuilder();

        // find out how to go back to the root
        href.append( this.getPackageRoot() );

        // now find out how to get to the dest package
        dest = dest.replace( ".*", "" ).replace( '.', '/' );

        href.append( dest );

        // Now append filename.html
        if ( jc != null )
        {
            href.append( '/' );
            href.append( jc.getFilename() );
            href.append( ".html" );
            href.append( '#' );
            href.append( jc.getName() );
        }

        return href.toString();
    }

    /**
     * Based on the destination package, get the HREF.
     *
     * @param dest destination
     * @return href
     */
    private String getHREF( String dest )
    {
        return getHREF( dest, null );
    }

    /**
     * <p>
     * Given the name of a package... get the number of subdirectories/subpackages there would be.
     * </p>
     * <p>
     * EX: {@code org.apache.maven == 3}
     * </p>
     *
     * @param packageName String
     * @return int
     */
    private int getPackageCount( String packageName )
    {
        if ( packageName == null )
        {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ( true )
        {
            index = packageName.indexOf( '.', index );

            if ( index == -1 )
            {
                break;
            }
            ++index;
            ++count;
        }

        // need to increment this by one
        ++count;

        return count;
    }

    /**
     * Parse out the current link and look for package/import statements and then create HREFs for them
     *
     * @param line line
     * @return processed line
     */
    private String importFilter( String line )
    {
        int start = -1;

        /*
         * Used for determining if this is a package declaration. If it is then we can make some additional assumptions:
         * - that this isn't a Class import so the full String is valid - that it WILL be on the disk since this is
         * based on the current - file.
         */
        boolean isPackage = line.trim().startsWith( "package " );
        boolean isImport = line.trim().startsWith( "import " );

        if ( isImport || isPackage )
        {
            start = line.trim().indexOf( ' ' );
        }

        if ( start != -1 )
        {
            // filter out this packagename...
            String pkg = line.substring( start ).trim();

            // specify the classname of this import if any.
            String classname = null;

            if (pkg.contains(".*"))
            {
                pkg = pkg.replace( ".*", "" );
            }
            else if ( !isPackage )
            {
                // this is an explicit Class import

                String packageLine = pkg;

                // This catches a boundary problem where you have something like:
                //
                // Foo foo = FooMaster.getFooInstance().
                // danceLittleFoo();
                //
                // This breaks Jxr and won't be a problem when we hook
                // in the real parser.

                int a = packageLine.lastIndexOf( '.' ) + 1;
                int b = packageLine.length() - 1;

                if ( a > b + 1 )
                {
                    classname = packageLine.substring( packageLine.lastIndexOf( '.' ) + 1, packageLine.length() - 1 );

                    int end = pkg.lastIndexOf( '.' );
                    if ( end == -1 )
                    {
                        end = pkg.length() - 1;
                    }

                    pkg = pkg.substring( 0, end );
                }
            }

            pkg = pkg.replace( ";", "" );
            String pkgHREF = getHREF( pkg );
            // if this package is within the PackageManager then you can create an HREF for it.

            if ( packageManager.getPackageType( pkg ) != null || isPackage )
            {
                // Create an HREF for explicit classname imports
                if ( classname != null )
                {
                    line = line.replace( classname, "<a href=\"" + pkgHREF + '/' + classname + ".html"
                        + "\">" + classname + "</a>" );
                }

                // now replace the given package with a href
                line =
                    line.replace( pkg, "<a href=\"" + pkgHREF + '/' + DirectoryIndexer.INDEX + "\">" + pkg
                        + "</a>" );
            }

        }

        return line;
    }

    /**
     * if the given char is not one of the following in VALID_URI_CHARS then return true
     *
     * @param c char to check against VALID_URI_CHARS list
     * @return {@code true} if c is a valid URI char
     */
    private boolean isInvalidURICharacter( char c )
    {
        for ( char validUriChar : VALID_URI_CHARS )
        {
            if ( validUriChar == c )
            {
                return false;
            }
        }

        return true;
    }
}
