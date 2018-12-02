package com.bo.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import com.bo.keysandvalues.dataprocessing.Parser;

public class MockParser implements Parser {

	private Function<String, List<Entry<String, String>>> parser;

    @Override
	public List<Entry<String, String>> parse(String input) throws IllegalArgumentException 
	{
		if (this.parser == null)
		{
			return parseNothing(input);
		}
		return this.parser.apply(input);
	}

	/**
	 * @param parser the Parser to set
	 */
	public void setParser(Function<String, List<Entry<String, String>>> parser) 
	{
		this.parser = parser;
	}

	private static List<Entry<String, String>> parseNothing(String input)
	{
		return new ArrayList<>();
	}

}