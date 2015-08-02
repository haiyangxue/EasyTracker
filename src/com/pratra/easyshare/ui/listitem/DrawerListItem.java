package com.pratra.easyshare.ui.listitem;

import android.graphics.drawable.Drawable;

public class DrawerListItem {

	private Drawable icon;
	private String title;

	public DrawerListItem() {
	}

	public DrawerListItem(Drawable icon, String title) {
		this.icon = icon;
		this.title = title;
	}

	public Drawable getIcon() {
		return icon;
	}

	public String getTitle() {
		return title;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
