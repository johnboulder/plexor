package com.wherethisgo.plexor;

public class CannotPlaceValueException extends Exception
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	//Parameterless Constructor
	public CannotPlaceValueException()
	{
	}

	//Constructor that accepts a message
	public CannotPlaceValueException(String message)
	{
		super(message);
	}
}
