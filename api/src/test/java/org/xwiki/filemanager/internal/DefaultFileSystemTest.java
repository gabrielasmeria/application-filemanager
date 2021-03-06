/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.filemanager.internal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.filemanager.File;
import org.xwiki.filemanager.FileSystem;
import org.xwiki.filemanager.Folder;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Unit tests for {@link DefaultFileSystem}.
 * 
 * @version $Id$
 * @since 2.0M1
 */
public class DefaultFileSystemTest
{
    @Rule
    public MockitoComponentMockingRule<FileSystem> mocker = new MockitoComponentMockingRule<FileSystem>(
        DefaultFileSystem.class);

    private XWikiContext xcontext;

    private ComponentManager componentManager;

    @Before
    public void configure() throws Exception
    {
        xcontext = mock(XWikiContext.class);
        Provider<XWikiContext> xcontextProvider = mocker.getInstance(XWikiContext.TYPE_PROVIDER);
        when(xcontextProvider.get()).thenReturn(xcontext);

        XWiki wiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(wiki);
        when(xcontext.getUserReference()).thenReturn(new DocumentReference("wiki", "Users", "mflorea"));

        componentManager = mock(ComponentManager.class);
        Provider<ComponentManager> componentManagerProvider =
            mocker.getInstance(new DefaultParameterizedType(null, Provider.class, ComponentManager.class));
        when(componentManagerProvider.get()).thenReturn(componentManager);
    }

    @Test
    public void getFolder() throws Exception
    {
        DocumentReference folderReference = new DocumentReference("wiki", "Drive", "Folder");
        XWikiDocument folderDocument = mock(XWikiDocument.class);
        when(xcontext.getWiki().getDocument(folderReference, xcontext)).thenReturn(folderDocument);
        when(folderDocument.isNew()).thenReturn(false);

        DefaultFolder expectedFolder = spy(new DefaultFolder());
        when(componentManager.getInstance(Folder.class)).thenReturn(expectedFolder);

        Folder actualFolder = mocker.getComponentUnderTest().getFolder(folderReference);

        assertSame(expectedFolder, actualFolder);
        verify(expectedFolder).setDocument(folderDocument);
    }

    @Test
    public void getFile() throws Exception
    {
        DocumentReference fileReference = new DocumentReference("wiki", "Drive", "file.txt");
        XWikiDocument fileDocument = mock(XWikiDocument.class);
        when(xcontext.getWiki().getDocument(fileReference, xcontext)).thenReturn(fileDocument);
        when(fileDocument.isNew()).thenReturn(false);

        DefaultFile expectedFile = spy(new DefaultFile());
        when(componentManager.getInstance(File.class)).thenReturn(expectedFile);

        File actualFile = mocker.getComponentUnderTest().getFile(fileReference);

        assertSame(expectedFile, actualFile);
        verify(expectedFile).setDocument(fileDocument);
    }

    @Test
    public void saveFile() throws Exception
    {
        DefaultFile file = spy(new DefaultFile());
        XWikiDocument xdoc = mock(XWikiDocument.class);
        when(file.getDocument()).thenReturn(xdoc);

        mocker.getComponentUnderTest().save(file);

        verify(file).updateParentReferences();
        verify(xdoc).setAuthorReference(xcontext.getUserReference());
        verify(xcontext.getWiki()).saveDocument(xdoc, "", false, xcontext);
    }
}
