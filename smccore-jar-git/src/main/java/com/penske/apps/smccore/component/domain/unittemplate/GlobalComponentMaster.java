/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.unittemplate;

/**
 * 
 */
//FIXME: document
public class GlobalComponentMaster extends UnitComponentMaster
{
	/** Null constructor - MyBatis only */
	protected GlobalComponentMaster() {}
	
	public GlobalComponentMaster(GlobalComponentMaster source, int masterId)
	{
		super(source, masterId);
	}
}
