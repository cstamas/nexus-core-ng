/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;

import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;



public class LdapUserDAOTest
    extends AbstractLdapTestEnvironment
{

    public void testSimple()
        throws Exception
    {

        Map<String, Object> env = new HashMap<String, Object>();
        // Create a new context pointing to the overseas partition
        env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        env.put( Context.PROVIDER_URL, "ldap://localhost:12345/o=sonatype" );
        env.put( Context.SECURITY_PRINCIPAL, "uid=admin,ou=system" );
        env.put( Context.SECURITY_CREDENTIALS, "secret" );
        env.put( Context.SECURITY_AUTHENTICATION, "simple" );

        // if want to use explicitly ApacheDS and not the Sun supplied ones
        // env.put( Context.PROVIDER_URL, "o=sonatype" );
        // env.put( Context.INITIAL_CONTEXT_FACTORY, "org.apache.directory.server.jndi.ServerContextFactory" );

        InitialLdapContext initialContext = new InitialLdapContext( new Hashtable<String, Object>( env ), null );

        LdapAuthConfiguration configuration = new LdapAuthConfiguration();
        configuration.setUserBaseDn( "ou=people" );
        configuration.setGroupBaseDn( "ou=groups" );
        configuration.setGroupObjectClass( "groupOfUniqueNames" );
        configuration.setGroupMemberAttribute( "uniqueMember" );
        configuration.setUserRealNameAttribute( "cn" );

        LdapUserDAO lum = (LdapUserDAO) lookup( LdapUserDAO.class.getName() );

        LdapUser user = lum.getUser( "cstamas", initialContext, configuration );
        assertEquals( "cstamas", user.getUsername() );
        // assertEquals( "Tamas Cservenak", user.getRealName() );
        assertEquals( "cstamas123", user.getPassword() );

        user = lum.getUser( "brianf", initialContext, configuration );
        assertEquals( "brianf", user.getUsername() );
        // assertEquals( "Brian Fox", user.getRealName() );
        assertEquals( "brianf123", user.getPassword() );

        user = lum.getUser( "jvanzyl", initialContext, configuration );
        assertEquals( "jvanzyl", user.getUsername() );
        // assertEquals( "Jason Van Zyl", user.getRealName() );
        assertEquals( "jvanzyl123", user.getPassword() );

        try
        {
            user = lum.getUser( "intruder", initialContext, configuration );
            fail();
        }
        catch ( NoSuchLdapUserException e )
        {
            // good
        }

    }

}
