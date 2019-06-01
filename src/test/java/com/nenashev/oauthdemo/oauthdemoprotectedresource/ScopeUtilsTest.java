package com.nenashev.oauthdemo.oauthdemoprotectedresource;

import com.nenashev.oauthdemo.oauthdemoprotectedresource.util.ScopeUtils;

import org.junit.Assert;
import org.junit.Test;

public class ScopeUtilsTest {

    @Test
    public void testHasAccess() {
        final String scopeRead = "read";

        Assert.assertTrue(ScopeUtils.hasAccess(scopeRead, "read"));
        Assert.assertFalse(ScopeUtils.hasAccess(scopeRead, "write"));
        Assert.assertFalse(ScopeUtils.hasAccess(scopeRead, "delete"));

        final String scopeReadWrite = "read write";

        Assert.assertTrue(ScopeUtils.hasAccess(scopeReadWrite, "read"));
        Assert.assertTrue(ScopeUtils.hasAccess(scopeReadWrite, "write"));
        Assert.assertFalse(ScopeUtils.hasAccess(scopeReadWrite, "delete"));

        final String scopeReadWriteDelete = "read write delete";

        Assert.assertTrue(ScopeUtils.hasAccess(scopeReadWriteDelete, "read"));
        Assert.assertTrue(ScopeUtils.hasAccess(scopeReadWriteDelete, "write"));
        Assert.assertTrue(ScopeUtils.hasAccess(scopeReadWriteDelete, "delete"));
    }
}
