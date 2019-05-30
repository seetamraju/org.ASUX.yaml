/*
 BSD 3-Clause License
 
 Copyright (c) 2019, Udaybhaskar Sarma Seetamraju
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 * Neither the name of the copyright holder nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.yaml.CollectionsImpl;

import org.ASUX.common.Tuple;
import org.ASUX.common.Output;

import org.ASUX.yaml.YAML_Libraries;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 *  <p>This class is a bunch of tools to help make it easy to work with the java.util.Map objects that the YAML library creates.</p>
 *  <p>One example is the work around required when replacing the 'Key' - within the MACRO command Processor.</p>
 *  <p>If the key is already inside single or double-quotes.. then the replacement ends up as <code>'"newkeystring"'</code></p>
 */
public class Tools extends org.ASUX.yaml.Tools {

    public static final String CLASSNAME = Tools.class.getName();
    private CmdInvoker cmdInvoker;

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
      * <p>Utility class for use within the org.ASUX.yaml library only</p><p>one of 2 constructors - public/private/protected</p>
      * @param _verbose Whether you want deluge of debug-output onto System.out.
      */
    public Tools(boolean _verbose ) {
        super( _verbose );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * Allows this class to get the YAML-Library specific implementation details from the appropriate subclass of CmdInvoker
     * @param _cmdInvoker instance of org.ASUX.yaml.CmdInvoker - specifically its subclasses org.ASUX.yaml.CollectionsImpl.CmdInvoker / org.ASUX.yaml.NodeImpl.CmdInvoker
     */
    public void setCmdInvoker( final org.ASUX.yaml.CmdInvoker _cmdInvoker ) {
        this.cmdInvoker = (CmdInvoker)_cmdInvoker;// I know what I'm doing...
        // This Tools class and cmdInvoker should belong in the SAME PACKAGE.
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Takes any JSON input - as a LinkedHashmap obtained from any Library - and exports it as YAML (to java.util.String), and then reads it back as YAML.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _map a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public LinkedHashMap<String, Object> lintRemoverMap( final LinkedHashMap<String, Object> _map ) throws Exception
    {
        // First write it to java.lang.String object... then, read it back into YAML, using the YamlReder class
        try {
            final String s = Map2YAMLString( _map );
            return YAMLString2Map( s, false ); // 2nd parameter is 'bWrapScalar' === false;.  's' cannot be a scalar at this point.  If it is, I want things to fail with null-pointer.

        } catch (java.io.IOException e) {
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": JSON2YAML(): Failure to read/write the contents of the String '" + _map.toString() +"'.");
            throw e;
        } catch (Exception e) {
            if ( this.verbose ) e.printStackTrace(System.err);
            if ( this.verbose ) System.err.println( CLASSNAME + ": JSON2YAML(): Unknown Internal error:.");
            throw e;
        }
        // return null;
    } // function

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** Takes any YAML input - as a LinkedHashmap - and exports it as YAML-String (to java.util.String)
     *  @param _yaml a java.util.LinkedHashMap&lt;String, Object&gt; object, as generated by Jackson http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     *  @throws Exception Any issue whatsoever when dealing with convering YAML/JSON content into Strings and back (as part of lintremoval)
     */
    public String Map2YAMLString( final Object _yaml ) throws Exception
    {
        final GenericYAMLWriter writer = new GenericYAMLWriter( this.verbose );
        writer.setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );
        final java.io.StringWriter javawriter = new java.io.StringWriter();
        writer.prepare( javawriter );
        writer.write( _yaml );
        writer.close();
        javawriter.close();
        return javawriter.toString();
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  Takes any STRING-form JSON as input - it better be valid JSON - and reads it back as YAML/LinkedHashMap.
     *  I need such a function, as I learned the hard way that libraries do NOT work 100% well.  Only file-formats are the workaorund/ way out.
     *  I definitely "fgool-proof" method to ensure 'valid' YAML, for error-free processing by the entire org.ASUX.yaml library to work without any issues
     *  @param _yamlString a java.lang.String object
     *  @param _bWrapScalar true or false.  If the returne value is going to be a SCALAR, do you want it wrapped into a LinkedHashMap or throw instead?
     *  @return a java.util.LinkedHashMap&lt;String, Object&gt; object that's definitely "kosher" for the entire org.ASUX.yaml library to work without any issues
     * @throws java.io.IOException if any error using java.io.StringReader and java.io.StringWriter
     * @throws Exception any other run-time exception, while parsing large Strings, nullpointers, etc.. ..
     */
    public LinkedHashMap<String, Object>  YAMLString2Map( final String  _yamlString, final boolean _bWrapScalar )
                    throws java.io.IOException, Exception
    {
        if ( this.verbose ) System.out.println(">>>>>>>>>>>>>>>>>>>> "+ CLASSNAME+": YAMLString2Map(): "+ _yamlString);

        try {
            final java.io.Reader reader3 = new java.io.StringReader( _yamlString );
            final org.ASUX.common.Output.Object<?> outpObj = this.cmdInvoker.getYamlScanner().load( reader3 );
            final LinkedHashMap<String, Object> tempMap = outpObj.getMap();
            reader3.close();
            if ( this.verbose ) System.out.println( CLASSNAME + ": YAMLString2Map(): created new Map = " + tempMap.toString() +" " );

            return tempMap;

        } catch (Exception e) {
            // YamlReader$YamlReaderException: Line 0, column 10: Expected data for a java.util.LinkedHashMap field but found: scalar
            if (this.verbose) System.out.println( CLASSNAME+": YAMLString2Map(): Hmmm.. Just a string?? passed as parameter??");
            // if ( e.getMessage().contains("Expected data for a java.util.LinkedHashMap field but found: scalar"))
            if ( e.getMessage().contains("but found: scalar") && _bWrapScalar )
                return new Output(this.verbose).wrapAnObject_intoLinkedHashMap( _yamlString );
            else {
                if ( this.verbose ) e.printStackTrace(System.err);
                if ( this.verbose ) System.err.println( CLASSNAME+": YAMLString2Map(): Input String ["+ _yamlString +"] does Not seem to be YAML - nor - a simple SCALAR string" );
                throw e;
            }
        }
    } // function

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================


    public static void main( String[] args ) {
        try {
            final GenericYAMLScanner rdr = new GenericYAMLScanner(true);
            rdr.setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );
            final GenericYAMLWriter wr = new GenericYAMLWriter(true);
            wr.setYamlLibrary( YAML_Libraries.ESOTERICSOFTWARE_Library );
            final Tools tools = new Tools( true );
            // tools.cmdInvoker
            LinkedHashMap<String, Object> map = tools.JSONString2Map( args[0] );
            System.out.println("Normal completion of program");
        } catch (java.io.IOException e) {
            e.printStackTrace(System.err);
            System.exit(102);
        } catch (Exception e) {
            if ( e.getMessage().contains("but found: scalar" ) ) {
                System.out.println("\n\n Just a string!" );
            } else {
                e.printStackTrace(System.err);
                System.exit(103);
            }
        }
        System.exit(0);
    }

}
