package org.sonatype.aether.ant;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public final class ArrayContainsAtLeastOnce
    extends BaseMatcher<Object[]>
{
    private Matcher<?> matcher;

    public ArrayContainsAtLeastOnce( Matcher<? extends Object> matcher )
    {
        this.matcher = matcher;
    }

    public boolean matches( Object item )
    {
        Object[] items = (Object[]) item;
        for ( Object string : items )
        {
            if ( matcher.matches( string ) )
            {
                return true;
            }
        }
        return false;
    }

    public void describeTo( Description description )
    {
        description.appendText( "Array containing at least once " );
        description.appendDescriptionOf( matcher );
    }
}