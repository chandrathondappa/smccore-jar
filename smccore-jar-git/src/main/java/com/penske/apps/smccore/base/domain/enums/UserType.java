/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.domain.enums;

/**
 * Differentiates between vendor users and Penske users
 */
public enum UserType implements MappedEnum
{
	PENSKE(1, "Penske"),
	VENDOR(2, "Vendor");
	
	private final int typeId;
	private final String label;
	
	private UserType(int typeId, String label)
	{
		this.typeId = typeId;
		this.label = label;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMappedValue()
	{
		return String.valueOf(typeId);
	}
		
	//***** DEFAULT ACCESSORS *****//
	public int getTypeId()
	{
		return typeId;
	}

	public String getLabel()
	{
		return label;
	}	
}
