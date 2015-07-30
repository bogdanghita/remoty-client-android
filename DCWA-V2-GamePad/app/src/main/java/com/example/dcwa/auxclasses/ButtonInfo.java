package com.example.dcwa.auxclasses;

public class ButtonInfo {

	private int layoutId;
	private int nameId;
	private int actionId;
	private String name;
	private String action;
	private String actionName;

	private int pRowStart, pRowStop, pColumnStart, pColumnStop, lRowStart, lRowStop, lColumnStart, lColumnStop;
	
	public ButtonInfo(String name, String action, String actionName, int layoutId, int nameId, int actionId,
			int pRowStart, int pRowStop,int pColumnStart, int pColumnStop,
			int lRowStart, int lRowStop,int lColumnStart, int lColumnStop) {
		
		this.name = name;
		this.action = action;
		this.actionName = actionName;
		
		this.layoutId = layoutId;
		this.nameId = nameId;
		this.actionId = actionId;
		
		this.pRowStart = pRowStart;
		this.pRowStop = pRowStop;
		this.pColumnStart = pColumnStart;
		this.pColumnStop = pColumnStop;
		
		this.lRowStart = lRowStart;
		this.lRowStop = lRowStop;
		this.lColumnStart = lColumnStart;
		this.lColumnStop = lColumnStop;
	}
	
	public int getLayoutId() {
		return layoutId;
	}
	public int getNameId() {
		return nameId;
	}
	public int getActionId() {
		return actionId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	
	public String getName() {
		return name;
	}
	public String getAction() {
		return action;
	}
	public String getActionName() {
		return actionName;
	}
	
	public int getPRowStart() {
		return pRowStart;
	}
	public int getPRowStop() {
		return pRowStop;
	}
	public int getPColumnStart() {
		return pColumnStart;
	}
	public int getPColumnStop() {
		return pColumnStop;
	}
	
	public int getLRowStart() {
		return lRowStart;
	}
	public int getLRowStop() {
		return lRowStop;
	}
	public int getLColumnStart() {
		return lColumnStart;
	}
	public int getLColumnStop() {
		return lColumnStop;
	}
}
