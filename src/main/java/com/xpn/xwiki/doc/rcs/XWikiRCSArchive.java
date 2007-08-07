/*
 * Copyright 2007, XpertNet SARL, and individual contributors.
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
 *
 */
package com.xpn.xwiki.doc.rcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.tools.ant.filters.StringInputStream;
import org.suigeneris.jrcs.rcs.Archive;
import org.suigeneris.jrcs.rcs.InvalidTrunkVersionNumberException;
import org.suigeneris.jrcs.rcs.Version;
import org.suigeneris.jrcs.rcs.impl.Node;
import org.suigeneris.jrcs.rcs.impl.TrunkNode;
import org.suigeneris.jrcs.rcs.parse.ParseException;
import org.suigeneris.jrcs.util.ToString;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Class for String [de]serialization for {@link XWikiDocumentArchive}.
 * @version $Id: $
 */
public class XWikiRCSArchive extends Archive
{
    /**
     * Used to serialize {@link XWikiDocumentArchive}.
     * @param nodeInfos - collection of {@link XWikiRCSNodeInfo} in any order
     * @param context - for loading nodes content 
     * @throws XWikiException if can't load nodes content
     * */
    public XWikiRCSArchive(Collection nodeInfos, XWikiContext context) throws XWikiException
    {
        super(new Object[0], "");
        nodes.clear();
        head = null;
        if (nodeInfos.size() > 0) {
            for (Iterator it = nodeInfos.iterator(); it.hasNext();) {
                XWikiRCSNodeInfo nodeinfo = (XWikiRCSNodeInfo) it.next();
                XWikiJRCSNode node = new XWikiJRCSNode(nodeinfo.getId().getVersion(), null);
                node.setAuthor(nodeinfo.getAuthor());
                node.setDate(nodeinfo.getDate());
                node.setLog(nodeinfo.getComment());
                node.setDiff(nodeinfo.isDiff());
                XWikiRCSNodeContent content = nodeinfo.getContent(context);
                node.setText(content.getPatch().getContent());
                nodes.put(node.getVersion(), node);
            }
            XWikiJRCSNode last = null;
            for (Iterator it = nodes.keySet().iterator(); it.hasNext();) {
                Version ver = (Version) it.next();
                XWikiJRCSNode node = (XWikiJRCSNode) nodes.get(ver);
                if (last != null) {
                    last.setRCSNext(node);
                }
                last = node;
                if (head == null) {
                    head = node;
                }
            }
        }
    }
    /**
     * Used to deserialize {@link XWikiDocumentArchive}.
     * @param archivetext - archive text in JRCS format
     * @throws ParseException if syntax errors
     */
    public XWikiRCSArchive(String archivetext) throws ParseException
    {
        super("", new StringInputStream(archivetext));
    }
    /**
     * Helper class for convert from {@link XWikiRCSNodeInfo} to JRCS {@link Node}.
     */
    private static class XWikiJRCSNode extends TrunkNode
    {
        /** bug if author=="". */
        public static String sauthorIfEmpty = "_";
        /** mark that node contains not patch. */
        public static String sfullVersion = "full";
        /** 
         * @param vernum - version of node
         * @param next - next node (with smaller version) in history
         * @throws InvalidTrunkVersionNumberException if version is invalid
         */
        public XWikiJRCSNode(Version vernum, TrunkNode next)
            throws InvalidTrunkVersionNumberException
        {
            super(vernum, next);
        }
        /** @param other - create class from copying this node */ 
        public XWikiJRCSNode(Node other)
        {
            this(other.version, null);
            this.date = other.getDate();
            this.author = other.getAuthor();
            this.state = other.getState();
            this.log = other.getLog();
            this.locker = other.getLocker();
            this.setText(other.getText());
        }
        /** @param date - date of modification */
        public void setDate(Date date) {
            this.date = date;
        }
        /** {@inheritDoc} */
        public void setAuthor(String user)
        {
            // 
            if (user == null || "".equals(user)) {
                super.setAuthor(sauthorIfEmpty);
            } else {
                super.setAuthor(user);
            }
        }
        /** @return is this node store diff or full version */
        public boolean isDiff() {
            // we need something filed in Node. locker is free
            return !sfullVersion.equals(getState());            
        }
        /** @param isdiff - true if node stores a diff, false - if full version */
        public void setDiff(boolean isdiff) {            
            setState(isdiff ? "diff" : sfullVersion);
        }
    }
    
    /**
     * @return Collection of pairs [{@link XWikiRCSNodeInfo},  {@link XWikiRCSNodeContent}]
     * @param docId - docId which will be wrote in {@link XWikiRCSNodeId#setDocId(long)}
     */
    public Collection getNodes(long docId) {
        Collection result = new ArrayList(nodes.values().size());
        for (Iterator it = nodes.values().iterator(); it.hasNext();) {
            XWikiJRCSNode node = new XWikiJRCSNode((Node) it.next());
            XWikiRCSNodeInfo nodeinfo = new XWikiRCSNodeInfo();
            nodeinfo.setId(new XWikiRCSNodeId(docId, node.getVersion()));
            nodeinfo.setAuthor(XWikiJRCSNode.sauthorIfEmpty.equals(node.getAuthor())
                ? "" : node.getAuthor());
            nodeinfo.setComment(node.getLog());
            nodeinfo.setDate(node.getDate());
            nodeinfo.setDiff(node.isDiff());
            XWikiRCSNodeContent content = new XWikiRCSNodeContent(nodeinfo.getId());
            content.setPatch(new XWikiPatch(ToString.arrayToString(node.getText()), node.isDiff()));
            nodeinfo.setContent(content);
            result.add(nodeinfo);
            result.add(content);
        }
        // ensure latest version is full
        ((XWikiRCSNodeInfo) result.iterator().next()).setDiff(false);
        return result;
    }
}
