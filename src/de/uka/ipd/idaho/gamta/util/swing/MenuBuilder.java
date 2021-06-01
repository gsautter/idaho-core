/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.gamta.util.swing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Utility class organizing menu items into a hierarchy of menus and sub menus
 * based upon line based configuration data. The configuration lists can also
 * define structuring elements of a menu:<UL>
 * <LI>Use '<code>---</code>' to indicate a separator line for grouping menu
 * items</LI>
 * <LI>Use '<code>+ &lt;label&gt;</code>' as a prefix to start a new sub menu
 * labeled <code>&lt;label&gt;</code></LI>
 * <LI>Use '<code>- &lt;itemName&gt;</code>' as a prefix to to include an item
 * with text <code>&lt;itemName&gt;</code> in a previously opened sub menu</LI>
 * </UL>
 * Sub menus can be cascaded. The start of a new sub menu automatically closes
 * any sub menu previously opened at the same level, as does an item name not
 * prefixed with <code>- </code> at the same level.<BR>
 * Sub menus with a single contained item are flattened out to that very item,
 * and recursively so; empty sub menus are discarded.
 * 
 * @author sautter
 */
public class MenuBuilder {
	
	/**
	 * character sequence separating the text of a menu item from an
	 * associated description (with the latter becoming the tooltip text),
	 * namely  '<code>&lt;=?=</code>' - this constant is public mainly for
	 * documentation purposes */
	private static final String ITEM_NAME_DESCRIPTION_SEPARATOR = "<=?="; // assigning a help text to the item name ... of sorts
	
	//	TODO also offer this for HTML and JavaScript
	
	/**
	 * Create a menu and populate it with given menu items, organized based
	 * upon a list of entry names. Items not assigned a position via the
	 * argument list are placed at the end of the menu. The argument map is
	 * expected to contain <code>JMenuItem</code>s as values, the keys being
	 * the text of these exact menu items. The argument list of names of names
	 * is expected to contain the menu item texts used as keys in the map, in
	 * the order they are expected to appear in the produced menu, possibly
	 * with sub menu structuring prefixes and appended tooltip texts. It is
	 * recommended that the runtime type of the argument list support random
	 * access, line e.g. an <code>ArrayList</code>, and that the argument map
	 * be case sensitive.
	 * @param name the name (label) of the menu
	 * @param itemNames the list of menu item names defining the structure of
	 *            the menus
	 * @param itemsByName the items to place in the menu, indexed by name
	 *            (label)
	 * @return the menu
	 */
	public static JMenu buildMenu(String name, List itemNames, Map itemsByName, boolean verbose) {
		JMenu menu = new JMenu(name);
		addMenuItems(menu, itemNames, 0, "", itemsByName, new HashSet(), verbose);
		return menu;
	}
	
	/**
	 * Populate a menu with given menu items, organized based upon a list of
	 * entry names. Items not assigned a position via the argument list are
	 * placed at the end of the menu. The argument map is expected to contain
	 * <code>JMenuItem</code>s as values, the keys being the text of these
	 * exact menu items. The argument list of names of names is expected to
	 * contain the menu item texts used as keys in the map, in the order they
	 * are expected to be added to the argument menu, possibly with sub menu
	 * structuring prefixes and appended tooltip texts. It is recommended that
	 * the runtime type of the argument list support random access, line e.g.
	 * an <code>ArrayList</code>, and that the argument map be case sensitive.
	 * @param menu the menu to populate
	 * @param itemNames the list of menu item names defining the structure of
	 *            the menus
	 * @param itemsByName the items to place in the menu, indexed by name
	 *            (label)
	 */
	public static void fillMenu(JMenu menu, List itemNames, Map itemsByName, boolean verbose) {
		addMenuItems(menu, itemNames, 0, "", itemsByName, new HashSet(), verbose);
	}
	
	private static int addMenuItems(JMenu menu, List itemNames, int itemIndex, String itemNamePrefix, Map itemsByName, HashSet addedItemNames, boolean verbose) {
		boolean separatorBeforeItem = false;
		JMenuItem mi;
		
		//	add configured items first
		while (itemIndex < itemNames.size()) {
			String itemName = ((String) itemNames.get(itemIndex));
			if (verbose) System.out.println("Handling " + itemName);
			
			//	cut current prefix
			if (itemName.startsWith(itemNamePrefix)) {
				itemName = itemName.substring(itemNamePrefix.length());
				itemIndex++;
			}
			else return itemIndex; // continue one level up
			
			//	remember to add separator (only before next actual item)
			if ("---".equals(itemName)) {
				separatorBeforeItem = true;
				if (verbose) System.out.println(" ==> marked separator before next item");
				continue;
			}
			
			//	parse description (tooltip text) off item name
			String itemDescription = null;
			if (itemName.indexOf(ITEM_NAME_DESCRIPTION_SEPARATOR) != -1) {
				itemDescription = itemName.substring(itemName.indexOf(ITEM_NAME_DESCRIPTION_SEPARATOR) + ITEM_NAME_DESCRIPTION_SEPARATOR.length()).trim();
				itemName = itemName.substring(0, itemName.indexOf(ITEM_NAME_DESCRIPTION_SEPARATOR)).trim();
				if (verbose) {
					System.out.println(" - item name is " + itemName);
					System.out.println(" - description is " + itemDescription);
				}
			}
			
			//	start of new sub menu
			if (itemName.startsWith("+ ")) {
				if (verbose) System.out.println(" ==> creating sub menu " + itemName.substring("+ ".length()));
				JMenu subMenu = new JMenu(itemName.substring("+ ".length()));
				//	TODOne do we need the spaces ??? ==> most likely, for readability, especially with separators
				itemIndex = addMenuItems(subMenu, itemNames, itemIndex, (itemNamePrefix + "- "), itemsByName, addedItemNames, verbose);
				mi = flattenMenu(subMenu, verbose);
			}
			
			//	regular menu item
			else {
//				mi = ((JMenuItem) itemsByName.remove(itemName));
				mi = ((JMenuItem) itemsByName.get(itemName));
				if (mi != null)
					addedItemNames.add(itemName);
			}
			
			//	add item (basic or sub menu)
			if (mi != null) {
				if (separatorBeforeItem && (menu.getItemCount() != 0))
					menu.addSeparator();
				separatorBeforeItem = false;
				menu.add(mi);
				if (verbose) System.out.println(" ==> added " + itemName);
				if ((itemDescription != null) && (mi.getToolTipText() == null))
					mi.setToolTipText(itemDescription);
			}
			else if (verbose) System.out.println(" ==> unable to find " + itemName);
		}
		
		//	we're in a sub menu, so we're done here
		if (itemNamePrefix.length() != 0)
			return itemIndex;
		
		//	collect unassigned item names
		LinkedHashMap remainingItemsByName = null;
		for (Iterator init = itemsByName.keySet().iterator(); init.hasNext();) {
			String itemName = ((String) init.next());
			if (addedItemNames.contains(itemName))
				continue;
			if (remainingItemsByName == null)
				remainingItemsByName = new LinkedHashMap();
			remainingItemsByName.put(itemName, itemsByName.get(itemName));
		}
		
		//	anything left to add?
		if (remainingItemsByName == null)
			return itemIndex;
		
		//	add remaining items at top level
		if (menu.getItemCount() != 0)
			menu.addSeparator();
		//	TODOne test which arrangement looks better !!! ==> adding to main menu preferable ... more seamless for users
//		JMenu moreMenu = new JMenu("More ...");
		for (Iterator init = remainingItemsByName.keySet().iterator(); init.hasNext();) {
			String itemName = ((String) init.next());
			if (verbose) System.out.println("Unassigned: " + itemName);
			mi = ((JMenuItem) remainingItemsByName.get(itemName));
			if (mi != null)
				menu.add(mi);
//				moreMenu.add(mi);
		}
//		if (moreMenu.getItemCount() != 0)
//			menu.add(moreMenu);
		
		//	finally ...
		return itemIndex;
	}
	
	private static JMenuItem flattenMenu(JMenu menu, boolean verbose) {
		if (menu.getItemCount() == 0)
			return null;
		if (menu.getItemCount() > 1)
			return menu;
		JMenuItem mi = menu.getItem(0);
		if (mi instanceof JMenu)
			return flattenMenu(((JMenu) mi), verbose);
		else return mi;
	}
}
