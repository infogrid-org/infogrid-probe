//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.model.primitives;

import java.io.ObjectStreamException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.util.StringHelper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringRepresentationParseException;
import org.infogrid.util.text.StringifierException;

/**
  * This is an string DataType for PropertyValues. While this DataType does not limit
  * the length of a string, it is typically only used for "short" strings. For multi-line strings,
  * use BlobDataType with a text MIME type.
  */
public final class StringDataType
        extends
            DataType
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * This is the default instance of this class.
      */
    public static final StringDataType theDefault = new StringDataType(
            null,
            StringValue.create( "" ) );

    /**
     * Factory method. Always returns the same instance.
     *
     * @return the default instance of this class
     */
    public static StringDataType create()
    {
        return theDefault;
    }

    /**
     * Factory method.
     *
     * @param defaultValue the default value when this DataType is instantiated
     * @return the StringDataType
     */
    public static StringDataType create(
            StringValue defaultValue )
    {
        return new StringDataType( null, defaultValue );
    }

    /**
     * Factory method.
     *
     * @param regex a regular expression that values need to conform to, if any
     * @param defaultValue the default value when this DataType is instantiated
     * @return the StringDataType
     */
    public static StringDataType create(
            Pattern     regex,
            StringValue defaultValue )
    {
        if( regex == null ) {
            if( defaultValue == null || defaultValue.equals( theDefault.getDefaultValue() )) {
                return theDefault;
            }
        } else if( defaultValue == null ) {
            throw new IllegalArgumentException( "Must provide defaultValue if Pattern is given" );
        } else {
            Matcher m = regex.matcher( defaultValue.value() );
            if( !m.matches()) {
                throw new IllegalArgumentException( "Given defaultValue \"" + defaultValue.value() + "\" does not match StringDataType's regex \"" + regex.toString() + "\".");
            }
        }
        return new StringDataType( regex, defaultValue );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param regex a regular expression that values need to conform to, if any
     * @param defaultValue the default value when this DataType is instantiated
     */
    private StringDataType(
            Pattern     regex,
            StringValue defaultValue )
    {
        super( null );

        theRegex        = regex;
        theDefaultValue = defaultValue;
    }

    /**
      * Test for equality.
      *
      * @param other object to test against
      * @return true if the two objects are equal
      */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof StringDataType )) {
            return false;
        }

        StringDataType realOther = (StringDataType) other;
        if( theRegex != null ) {
            if( !theRegex.equals( realOther.theRegex )) {
                return false;
            }
        } else if( realOther.theRegex != null ) {
            return false;
        }
        if( theDefaultValue != null ) {
            return theDefaultValue.equals( realOther.theDefaultValue );
        } else {
            return realOther.theDefaultValue == null;
        }
    }

    /**
     * Determine hash code. Make editor happy that otherwise indicates a warning.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        int ret = 0;
        if( theRegex != null ) {
            ret ^= theRegex.hashCode();
        }
        if( theDefaultValue != null ) {
            ret ^= theDefaultValue.hashCode();
        }
        return ret;
    }

    /**
     * Determine whether this PropertyValue conforms to the constraints of this instance of DataType.
     *
     * @param value the candidate PropertyValue
     * @return 0 if the candidate PropertyValue conforms to this type. Non-zero values depend
     *         on the DataType; generally constructed by analogy with the return value of strcmp.
     * @throws ClassCastException if this PropertyValue has the wrong type (e.g.
     *         the PropertyValue is a StringValue, and the DataType an IntegerDataType)
     */
    public int conforms(
            PropertyValue value )
        throws
            ClassCastException
    {
        StringValue realValue = (StringValue) value; // may throw
        if( theRegex == null ) {
            return 0;
        }
        Matcher m = theRegex.matcher( realValue.value() );
        if( m.matches()) {
            return 0;
        }
        return Integer.MAX_VALUE;
    }

    /**
      * Determine whether a domain check shall be performed on
      * assignments. Default to false.
      *
      * @return if true, a domain check shall be performed prior to performing assignments
      */
    @Override
    public boolean getPerformDomainCheck()
    {
        return theRegex != null;
    }

    /**
      * Return a boolean expression in the Java language that uses
      * varName as an argument and that evaluates whether the content
      * of variable varName is assignable to a value of this data type.
      *
      * This is used primarily for code-generation purposes.
      * FIXME add support for units
      *
      * @param varName the name of the variable containing the value
      * @return the boolean expression
      */
    @Override
    public String getJavaDomainCheckExpression(
            String varName )
    {
        if( theRegex == null ) {
            return "true";
        }
        return "( java.util.Pattern.matches( " + varName + ", \"" + theRegex.toString() + "\" )";
    }

    /**
      * Obtain the Java class that can hold values of this data type.
      *
      * @return the Java class that can hold values of this data type
      */
    public Class getCorrespondingJavaClass()
    {
        return StringValue.class;
    }

    /**
     * Obtain the default value of this DataType.
     *
     * @return the default value of this DataType
     */
    public StringValue getDefaultValue()
    {
        return theDefaultValue;
    }

    /**
     * Correctly deserialize a static instance.
     *
     * @return the static instance if appropriate
     * @throws ObjectStreamException thrown if reading from the stream failed
     */
    public Object readResolve()
        throws
            ObjectStreamException
    {
        if( this.equals( theDefault )) {
            return theDefault;
        } else {
            return this;
        }
    }

    /**
     * Obtain a value expression in the Java language that invokes the constructor
     * of factory method of the underlying concrete class, thereby creating or
     * reusing an instance of the underlying concrete class that is identical
     * to the instance on which this method was invoked.
     *
     * This is used primarily for code-generation purposes.
     *
     * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
     * @return the Java language expression
     */
    public String getJavaConstructorString(
            String classLoaderVar )
    {
        final String className = getClass().getName();

        if( this == theDefault ) {
            return className + DEFAULT_STRING;
        } else {
            StringBuilder ret = new StringBuilder( className );
            ret.append( CREATE_STRING );

            if( theRegex != null ) {
                ret.append( "java.util.regex.Pattern.compile( \"" );
                ret.append( StringHelper.stringToJavaString( theRegex.toString() ));
                ret.append( "\" )" );
            } else {
                ret.append( NULL_STRING );
            }
            ret.append( COMMA_STRING );
            if( theDefaultValue != null ) {
                ret.append( theDefaultValue.getJavaConstructorString( classLoaderVar , null ));
            } else {
                ret.append( NULL_STRING );
            }

            ret.append( CLOSE_PARENTHESIS_STRING );
            return ret.toString();
        }
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        String ret;
        if( theRegex != null ) {
            ret = rep.formatEntry(
                    StringValue.class,
                    REGEX_ENTRY,
                    pars,
                    this,
                    PropertyValue.toStringRepresentationOrNull( theDefaultValue, rep, pars ), // presumably shorter, but we don't know
                    theRegex.toString(),
                    theSupertype );
        } else {
            ret = rep.formatEntry(
                    StringValue.class,
                    DEFAULT_ENTRY,
                    pars,
                    this,
                    PropertyValue.toStringRepresentationOrNull( theDefaultValue, rep, pars ), // presumably shorter, but we don't know
                    theSupertype );
        }
        return ret;
    }

    /**
     * Obtain a PropertyValue that corresponds to this PropertyType, based on the String representation
     * of the PropertyValue.
     * 
     * @param representation the StringRepresentation in which the String s is given
     * @param s the String
     * @param mimeType the MIME type of the representation, if known
     * @return the PropertyValue
     * @throws PropertyValueParsingException thrown if the String representation could not be parsed successfully
     */
    public StringValue fromStringRepresentation(
            StringRepresentation representation,
            String               s,
            String               mimeType )
        throws
            PropertyValueParsingException
    {
        try {
            Object [] found = representation.parseEntry( StringValue.class, StringRepresentation.DEFAULT_ENTRY, s, this );

            StringValue ret;
            switch( found.length ) {
                case 3:
                    ret = StringValue.create( (String) found[2] );
                    break;

                default:
                    throw new PropertyValueParsingException( this, representation, s );
            }
            int conforms = conforms( ret );
            if( conforms != 0 ) {
                throw new PropertyValueParsingException( this, representation, s, new DoesNotMatchRegexException( ret, this ));
            }

            return ret;

        } catch( StringRepresentationParseException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex.getFormatString(), ex );

        } catch( ParseException ex ) {
            throw new PropertyValueParsingException( this, representation, s, null, ex );

        } catch( ClassCastException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex );
        }
    }

    /**
     * The default value for this instance of StringDataType.
     */
    private final StringValue theDefaultValue;

    /**
     * The regular expression that a StringValue needs to conform to, if any.
     */
    private Pattern theRegex;

    /**
     * The entry in the resource files for a StringDataType with a regular expression, prefixed by the StringRepresentation's prefix.
     */
    public static final String REGEX_ENTRY = "RegexType";
}