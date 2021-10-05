/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.component.domain.enums;

/**
 * A strategy enum describing how the rules engine should fetch values from components that are not visible (i.e. {@link Visibility#isVisible()} returns false)
 * This also describes how the program expects to use the results of running the rules. For instance, if components that are not visible after rules run will be removed,
 * then the {@link #REMOVE} element should be used. If non-visible components will not be removed, then the {@link #KEEP} element should be used.
 */
public enum NotVisibleBehavior
{
	/** Treat non-visible components as still having a value */
	KEEP,
	/** Treat non-visible components as not having a value only if their visibility has been altered by a rule whose criteria involve a component the user changed. */
	REMOVE_IF_USER_INFLUENCED,
	/** Treat non-visible components as not having any value (i.e. not being available in the container anymore) */
	REMOVE,
	;
}
