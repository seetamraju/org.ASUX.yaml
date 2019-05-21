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
import org.ASUX.common.Debug;

import org.ASUX.yaml.YAML_Libraries;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

// import java.io.FileNotFoundException;
// import java.io.IOException;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;


// https://yaml.org/spec/1.2/spec.html#id2762107
// import org.yaml.snakeyaml.Yaml;
// import org.yaml.snakeyaml.composer.Composer;
// import org.yaml.snakeyaml.nodes.NodeTuple;
// import org.yaml.snakeyaml.nodes.NodeId;
// import org.yaml.snakeyaml.nodes.Node;
// import org.yaml.snakeyaml.nodes.ScalarNode;
// import org.yaml.snakeyaml.nodes.MappingNode;
// import org.yaml.snakeyaml.nodes.SequenceNode;
// import org.yaml.snakeyaml.constructor.Constructor;
// import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
// import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

// import org.yaml.snakeyaml.constructor.SafeConstructor;

//=================================================================================
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//=================================================================================

public class GenericYAMLWriter {

    public static final String CLASSNAME = GenericYAMLWriter.class.getName();

    private boolean verbose;

    // https://yaml.org/spec/1.2/spec.html#id2762107
    protected org.yaml.snakeyaml.Yaml snakeYaml;
    protected java.io.Writer snakeYamlWriter;

    protected com.esotericsoftware.yamlbeans.YamlWriter esotericsoftwareWriter = null;

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    private YAML_Libraries sYAMLLibrary = YAML_Libraries.ASUXYAML_Library;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     */
    public GenericYAMLWriter( final boolean _verbose ) {
        this.verbose = _verbose;
        init();
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * Tells you what internal implementation of the YAML read/parsing is, and by implication what the internal implementation for YAML-output generation is.
     * @return a reference to the YAML Library in use. See {@link YAML_Libraries} for legal values.
     */
    public YAML_Libraries getYamlLibrary() {
        return this.sYAMLLibrary;
    }

    /**
     * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
     * @param _l the YAML-library to use going forward. See {@link YAML_Libraries} for legal values to this parameter
     */
    public void setYamlLibrary( final YAML_Libraries _l ) {
        this.sYAMLLibrary = _l;
    }

    /**
     * Invoke this method to re-initialize this class, after completing a sequence of {@link #prepare} {@link #write} {@link close}
     */
    public void init() {
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  This method takes the java.io.Writer (whether StringWriter or FileWriter) and prepares the YAML library to write to it.
     *  WARNING!!! The EsotericSoftware's com.esotericsoftware.yamlbeans.YamlWriter implementation takes over stdout, and it will STOP working for all System.out.println();
     *  @param _javawriter StringWriter or FileWriter (cannot be null)
     *  @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void prepare( final java.io.Writer _javawriter ) throws Exception
    {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
                this.snakeYaml = new org.yaml.snakeyaml.Yaml( this.defaultConfigurationForSnakeYamlWriter() );
                this.snakeYamlWriter = _javawriter;

                // //-------------------------------------------------
                // Serializer serializer = new Serializer(new Emitter(output, dumperOptions), resolver, dumperOptions, rootTag);
                // try {
                //     serializer.open();
                //     serializer.serialize(node);
                //     // while (data.hasNext()) {
                //     //    Node node = representer.represent(data.next());
                //     //    serializer.serialize(node);
                //     // }
                //     serializer.close();
                // } catch (IOException e) {
                //     throw new YAMLException(e);
                // }
                // //-------------------------------------------------
                // org.yaml.snakeyaml.emitter.Emitter emitter = new org.yaml.snakeyaml.emitter.Emitter( _javawriter
                //                , new org.yaml.snakeyaml.DumperOptions() );
                // try {
                //     for ( org.yaml.snakeyaml.events.Event event : document) {
                //         emitter.emit(event);
                //     }
                //     fail("Loading must fail for " + files[i].getAbsolutePath());
                //     // System.err.println("Loading must fail for " +
                //     // files[i].getAbsolutePath());
                // } catch( org.yaml.snakeyaml.error.YAMLException e ) {
                // } catch (Exception e) {
                //     assertTrue(true);
                // }
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
                // prepare for output: whether it goes to System.out -or- to an actual output-file.
                this.esotericsoftwareWriter = new com.esotericsoftware.yamlbeans.YamlWriter( _javawriter );
                // WARNING!!! com.esotericsoftware.yamlbeans.YamlWriter takes over stdout, and it will STOP working for all System.out.println();

                this.defaultConfigurationForEsotericsoftwareYamlWriter( this.esotericsoftwareWriter ); // , cmdLineArgs.quoteType
                break;

            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    } //function

    public void test( final java.io.Writer _javawriter, final org.yaml.snakeyaml.nodes.Node _output ) throws Exception
    {
        //-------------------------------------------------
        final org.yaml.snakeyaml.DumperOptions dumperOptions = this.defaultConfigurationForSnakeYamlWriter();
        final org.yaml.snakeyaml.emitter.Emitter snakeemitter = new org.yaml.snakeyaml.emitter.Emitter( _javawriter, dumperOptions );
        final org.yaml.snakeyaml.resolver.Resolver resolver = new org.yaml.snakeyaml.resolver.Resolver();
        final org.yaml.snakeyaml.nodes.Tag tag = org.yaml.snakeyaml.nodes.Tag.STR;
        org.yaml.snakeyaml.serializer.Serializer serializer = new org.yaml.snakeyaml.serializer.Serializer(
                            snakeemitter, resolver, dumperOptions, tag );
        try {
            serializer.open();
            serializer.serialize( _output );
            // while (data.hasNext()) {
            //    Node node = representer.represent(data.next());
            //    serializer.serialize(node);
            // }
            serializer.close();
        } catch (java.io.IOException e) {
            throw new Exception(e);
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Write the YAML content (_output parameter) using the YAML-Library specified via {@link #setYamlLibrary} and to the java.io.Writer reference provided via {@link #prepare(java.io.Writer)}.
     * @param _output the content you want written out as a YAML file.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void write( final Object _output ) throws Exception
    {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // https://yaml.org/spec/1.2/spec.html#id2762107
                // per https://bitbucket.org/asomov/snakeyaml/src/tip/src/test/java/examples/CustomMapExampleTest.java
                // See also https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-collections
                if ( this.snakeYaml != null || this.snakeYamlWriter != null ) {
                    this.snakeYaml.dump( _output, this.snakeYamlWriter );
                } else {
                    throw new Exception( CLASSNAME +" write("+ this.getYamlLibrary() +"): cannot invoke write() before prepare()." );
                } // if esotericsoftwareWriter !=   null
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
                if (this.esotericsoftwareWriter != null) {
                    if ( _output instanceof LinkedHashMap || _output instanceof ArrayList || _output instanceof LinkedList || _output instanceof String ) {
                        if (this.verbose) System.out.println( CLASSNAME + ": write(): writing output " + _output + "]" );
                        if (this.verbose) System.out.println( CLASSNAME + ": write(): final output is of type " + _output.getClass().getName() + "]" );
                        this.esotericsoftwareWriter.write( _output );
                        // @SuppressWarnings("unchecked")
                        // final LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) _output;
                        // this.esotericsoftwareWriter.write(map);
                    } else {
                        throw new Exception( CLASSNAME +" write("+ this.getYamlLibrary() +"): output is Not of type LinkedHashMap.  It's ["+ ((_output==null)?"null":_output.getClass().getName()) +"]");
                    }
                } else {
                    throw new Exception( CLASSNAME +" write("+ this.getYamlLibrary() +"): cannot invoke write() before prepare()." );
                } // if esotericsoftwareWriter != null
                break;

            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Call this in exactly the way you'd close a file after writing to it.  This method should be called ONLY after {@link #write(Object)} will no longer be invoked.
     * @throws Exception if the YAML libraries have any issues with ERRORs inthe YAML or other issues.
     */
    public void close() throws Exception {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                // Nothing to close if we use new org.yaml.snakeyaml.Yaml().dump();
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
                if ( this.esotericsoftwareWriter != null )
                    esotericsoftwareWriter.close();
                this.esotericsoftwareWriter = null;
                break;

            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    public org.yaml.snakeyaml.DumperOptions defaultConfigurationForSnakeYamlWriter() throws Exception
    {
        final org.yaml.snakeyaml.DumperOptions dopt = new org.yaml.snakeyaml.DumperOptions(); // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java
        // dopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.SINGLE_QUOTED );
                                                // other value are: PLAIN(a.k.a. nothing), DOUBLE_QUOTED, FOLDED('>')
        // dopt.setIndent( 3 );
        dopt.setCanonical( false );
        dopt.setPrettyFlow( true );
        dopt.setDefaultFlowStyle( org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK ); // BLOCK, FLOW or AUTO
        // dopt.setWidth( 80 ); // default is 80
        dopt.setSplitLines( false ); // do NOT Split up long lines
        // dopt.setTags( Map<String, String> _tags);
        // Not yet available in latest release:- dopt.setNonPrintableStyle( org.yaml.snakeyaml.DumperOptions.NonPrintableStyle.ESCAPE ); // When String contains non-printable characters SnakeYAML convert it to binary data with the !!binary tag. Set this to ESCAPE to keep the !!str tag and escape the non-printable chars with \\x or \\u
        return dopt;
    }

    public void defaultConfigurationForEsotericsoftwareYamlWriter( com.esotericsoftware.yamlbeans.YamlWriter esotericsoftwareWriter ) throws Exception
                                            // com.esotericsoftware.yamlbeans.YamlConfig.Quote qtyp
    {
        // Leverage the appropriate YAMLReader library to load file-contents into a java.util.LinkedHashMap<String, Object>
        switch ( this.getYamlLibrary() ) {
            case NodeImpl_Library:
            case SNAKEYAML_Library:
                break;

            case CollectionsImpl_Library:
            case ESOTERICSOFTWARE_Library:
                // esotericsoftwareWriter.getConfig().writeConfig.setWriteRootTags(false); // Does NOTHING :-
                esotericsoftwareWriter.getConfig().writeConfig.setWriteClassname(
                        com.esotericsoftware.yamlbeans.YamlConfig.WriteClassName.NEVER); // I hate !<pkg.className> within YAML files. So does AWS I believe.
                // esotericsoftwareWriter.getConfig().writeConfig.setQuoteChar( qtyp );
                // esotericsoftwareWriter.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.Quote.NONE );
                // esotericsoftwareWriter.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.Quote.SINGLE );
                // esotericsoftwareWriter.getConfig().writeConfig.setQuoteChar( com.esotericsoftware.yamlbeans.YamlConfig.Quote.DOUBLE );

                // esotericsoftwareWriter.getConfig().setClassTag("Equals", Equals.class);
                // esotericsoftwareWriter.getConfig().setClassTag("Or", Or.class);
                // esotericsoftwareWriter.getConfig().setClassTag("Ref", Ref.class);
                break;

            case ASUXYAML_Library:
            default:
                final String es = CLASSNAME + ": prepare(): Unimplemented YAML-Library: " + this.getYamlLibrary();
                System.err.println( es );
                throw new Exception( es );
                // break;
        } // switch
    } // method

}