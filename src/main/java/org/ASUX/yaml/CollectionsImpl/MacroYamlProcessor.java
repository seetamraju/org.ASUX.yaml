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

import org.ASUX.common.Macros;
import org.ASUX.yaml.YAMLPath;

// import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Properties;

import java.util.regex.*;

/** <p>This abstract class was written to re-use code to query/traverse a YAML file.</p>
 *  <p>This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.</p>
 *  <p>This abstract class has 4 concrete sub-classes (representing YAML-COMMANDS to read/query, list, delete and replace).</p>
 *  <p>See full details of how to use this, in {@link Cmd} as well as the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project.</p>
 * @see ReadYamlEntry
 * @see ListYamlEntry
 * @see DeleteYamlEntry
 * @see ReplaceYamlEntry
 */
public class MacroYamlProcessor {

    public static final String CLASSNAME = MacroYamlProcessor.class.getName();

    /** <p>Whether you want deluge of debug-output onto System.out.</p><p>Set this via the constructor.</p>
     *  <p>It's read-only (final data-attribute).</p>
     */
    private final boolean verbose;

    /** <p>Whether you want a final SHORT SUMMARY onto System.out.</p><p>a summary of how many matches happened, or how many entries were affected or even a short listing of those affected entries.</p>
     */
	public final boolean showStats;

	private int changesMade = 0;

    /** The only Constructor.
     *  @param _verbose Whether you want deluge of debug-output onto System.out
     *  @param _showStats Whether you want a final summary onto console / System.out
     */
    public MacroYamlProcessor(boolean _verbose, final boolean _showStats) {
		this.verbose = _verbose;
		this.showStats = _showStats;
    }
    private MacroYamlProcessor(){
		this.verbose = false;
		this.showStats = true;
    }

    //------------------------------------------------------------------------------
    public static class MacroException extends Exception {
        private static final long serialVersionUID = 2L;
        public MacroException(String _s) { super(_s); }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    private static final String macroEval( final boolean _verbose, final String _s,
                                    final Properties _props, final LinkedHashMap<String,Properties> _allProps )
                                    throws Exception
    {
        final String HDR = CLASSNAME + ": macroEval("+_s+"): ";
        final String v1 = org.ASUX.common.Macros.eval( _verbose, _s, _props );
        if ( _verbose ) System.out.println( HDR +" lookup #1 on Properties = ["+ v1 + "]" );
        final String v2 = org.ASUX.common.Macros.eval( _verbose, v1, _allProps );
        if ( _verbose ) System.out.println( HDR +" lookup #2 for LinkedHashMap<String,Properties> = ["+ v2 + "]" );
        return v2;
    }

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    /** <p>This is a RECURSIVE-FUNCTION.  Make sure to pass in the right parameters.</p>
     *  <p>Note: this function expects you to pass in a 'new java.utils.LinkedHashMap&lt;String, Object&gt;()' as the 2nd parameter.  It will be 'filled' when function returns.</p>
     *  <p>This function returns true, if ANY occurance of ${ASUX::__} was detected and evaluated. If false, _inpMap and _outMap will be identical when function returns</p>
     *  @param _inpMap A java.utils.LinkedHashMap&lt;String, Object&gt; (created by YAMLReader classes from various libraries) containing the entire Tree representing the YAML file.
     *  @param _outpMap Pass in a new 'empty' java.utils.LinkedHashMap&lt;String, Object&gt;().  THis is what this function *RETURNS* after Macros are evalated within _inpMap
     *  @param _props can be null, otherwise an instance of {@link java.util.Properties}
     *  @param _allProps can be null, otherwise an instance of LinkedHashMap&lt;String,Properties&gt;
     *  @return true = whether at least one match of ${ASUX::} happened.
	 *  @throws MacroYamlProcessor.MacroException - thrown if any attempt to evaluate MACROs fails within org.ASUX.common.Macros.eval() functions
	 *  @throws Exception - forany other run time error (especially involving YAML issues)
     */
    public boolean recursiveSearch(
            final LinkedHashMap<String, Object> _inpMap,
			final LinkedHashMap<String,Object> _outpMap,
            final Properties _props,
            final LinkedHashMap<String,Properties> _allProps
    ) throws MacroYamlProcessor.MacroException, Exception {

        if ( (_inpMap == null) || (_outpMap==null) ) return false;

        boolean bChangesMade = false;
		// final Tools tool = new Tools( this.verbose );

        //--------------------------
        for (Object keyAsIs : _inpMap.keySet()) {

            final String key = macroEval( this.verbose, keyAsIs.toString(), _props, _allProps );
            assert( key != null );

            // Note: the lookup within _inpMap .. uses keyAsIs.  Not key.
			final Object rhsObj = _inpMap.get(keyAsIs);  // otherwise we'll inefficiently be doing _inpMap.get multiple times below.

			final String rhsStr = (rhsObj==null)?"null":rhsObj.toString(); // to make verbose logging code simplified
            if ( this.verbose ) System.out.println ( "\n"+ CLASSNAME +": recursiveSearch(): recursing @ YAML-file-location: "+ key +"/"+ keyAsIs +" = "+ rhsStr.substring(0,rhsStr.length()>181?180:rhsStr.length()) );

			if ( rhsObj == null ) continue; // perhaps the YAML line is simply key-only like..    Key:
			//--------------------------------------------------------
			// So.. we need to keep recursing (specifically for LinkedHashMap<String, Object> & ArrayList YAML elements)
			if ( rhsObj instanceof LinkedHashMap ) {

				final LinkedHashMap<String,Object> newMap1	= new LinkedHashMap<>(); // create an empty Map
				@SuppressWarnings("unchecked")
				final LinkedHashMap<String, Object> rhs	= (LinkedHashMap<String, Object>) rhsObj;
				bChangesMade = this.recursiveSearch( rhs, newMap1, _props, _allProps ); // recursion call

				_outpMap.put( key, newMap1 );
				// Why am I not simply doing:-       _outpMap.put( key, newMap1 )
				// Well: If the key != keyAsIs .. then .. the resulting entry in YAML outputfile is something like '"key"' (that is, a single+double-quote problem)
				// tool.addMapEntry( _outpMap, key, newMap1 );

			} else if ( rhsObj instanceof java.util.ArrayList ) {

				final ArrayList arr = (ArrayList) rhsObj;
				final ArrayList<Object> newarr = new ArrayList<>();

				// Loop thru the 'arr' Array
				for ( Object o: arr ) {
					// iterate over each element
					if ( o instanceof LinkedHashMap ) {
						final LinkedHashMap<String,Object> newMap2 = new LinkedHashMap<>();
						@SuppressWarnings("unchecked")
						final LinkedHashMap<String,Object> rhs22 = (LinkedHashMap<String,Object>) o;
						bChangesMade = this.recursiveSearch( rhs22, newMap2, _props, _allProps ); // recursion call
						newarr.add( newMap2 );
					} else if ( o instanceof java.lang.String ) {
						// by o.toString(), I'm cloning the String object.. .. so both _inpMap and _outpMap do NOT share the same String object
						newarr.add ( macroEval( this.verbose, o.toString(), _props, _allProps ) );
					} else {
						System.err.println( CLASSNAME +": recursiveSearch(): incomplete code #1: failure w Array-type '"+ o.getClass().getName() +"'");
						System.exit(92); // This is a serious failure. Shouldn't be happening.
					} // if-Else   o instanceof LinkedHashMap<String, Object> - (WITHIN FOR-LOOP)
				} // for Object o: arr

				_outpMap.put( key, newarr );
				// Well: If the key != keyAsIs .. then .. the resulting entry in YAML outputfile is something like '"key"' (that is, a single+double-quote problem)

			} else if ( rhsObj instanceof java.lang.String ) {
				// by rhsObj.toString(), I'm cloning the String object.. .. so both _inpMap and _outpMap do NOT share the same String object
				final String asis = rhsObj.toString();
				final String news = macroEval( this.verbose, asis, _props, _allProps );
				if (   !    asis.equals(news) ) this.changesMade ++;
				_outpMap.put( key, news );
				// Well: If the key != keyAsIs .. then .. the resulting entry in YAML outputfile is something like '"key"' (that is, a single+double-quote problem)

            } else {
				System.err.println( CLASSNAME +": recursiveSearch(): incomplete code #2: failure w Type '"+ ((rhsObj==null)?"null":rhsObj.getClass().getName()) +"'");
				System.exit(93); // This is a serious failure. Shouldn't be happening.
            }// if-else yamlPElemPatt.matcher()

        } // for loop   key: _inpMap.keySet()

        // Now that we looped thru all keys at current recursion level..
		// .. for now nothing to do here.
		if ( this.showStats ) System.out.println("# of changes made = "+ changesMade );

        return bChangesMade;
    }

}
