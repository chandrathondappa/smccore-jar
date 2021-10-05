/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain;

import com.penske.apps.smccore.component.domain.enums.ProgramComponent;
import com.penske.apps.smccore.component.domain.enums.Visibility;

/**
 * A visibility override that can get applied to a component if the corresponding rule is satisfied
 */
public class RuleOutcome
{
	/** Internal database ID for this object */
	private Integer cmpRulesId;
	/** The ID of the template this outcome is attached to. */
	private int templateId;
	/** The rule that has to be true for this outcome to get applied. */
	private int ruleId;
	/**
	 * The ID of the component that this outcome will affect. If the rule in question is true,
	 * and if no other outcome has affected the component yet, the component will get the visibility given by {@link #visibility}.
	 */
	private int componentId;
	/** The order in which the rule outcomes should be checked. The first one that has a rule evaluating to true will be applied, and the others will be skipped. */
	private int priority;
	/**
	 * The visibility that the component will have if this outcome is applied to it.
	 */
	private Visibility visibility;

	/** Null constructor - MyBatis only */
	protected RuleOutcome() {}

	/**
	 * Programmatically create a new rule outcome for use by the rules engine.
	 * This constructor is intended primarily for use by the internals of the rules engine, but it is possible in theory that application code could use this
	 * to construct programmatic rule outcomes, as well.
	 * @param templateId The template that this rule outcome applies to.
	 * @param ruleId The rule that will cause this outcome to occur if it evaluates to true.
	 * @param programComponent The component whose visibility will be changed by this outcome.
	 * @param priority The priority of this outcome. Outcomes with lower priority take precedence over outcomes with higher priority.
	 * @param visibility The visibility that the component will have if this outcome is applied to it.
	 */
	public RuleOutcome(int templateId, int ruleId, ProgramComponent programComponent, int priority, Visibility visibility)
	{
		if(visibility == null)
			throw new IllegalArgumentException("Visibility is required for RuleOutcome (Rule ID " + ruleId + ", Component ID " + componentId + ", Template ID " + templateId + ")");
		if(programComponent == null)
			throw new IllegalArgumentException("Target component is required for RuleOutcome (Rule ID " + ruleId + ", Component ID " + componentId + ", Template ID " + templateId + ")");
		
		this.templateId = templateId;
		this.ruleId = ruleId;
		this.componentId = programComponent.getComponentId();
		this.priority = priority;
		this.visibility = visibility;
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{RuleOutcome " + cmpRulesId + " (Rule ID: " + ruleId + ", Component ID: " + componentId + "), Priority " + priority + ": " + visibility.getDescription() + "}";
	}

	//***** DEFAULT ACCESSORS *****//
	/**
	 * @return the ruleId
	 */
	public int getRuleId()
	{
		return ruleId;
	}

	/**
	 * @return the componentId
	 */
	public int getComponentId()
	{
		return componentId;
	}

	/**
	 * @return the visibility
	 */
	public Visibility getVisibility()
	{
		return visibility;
	}

	/**
	 * @return the cmpRulesId
	 */
	public Integer getCmpRulesId()
	{
		return cmpRulesId;
	}

	/**
	 * @return the priority
	 */
	public int getPriority()
	{
		return priority;
	}

	public int getTemplateId()
	{
		return templateId;
	}
}
