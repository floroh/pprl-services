package de.unileipzig.dbs.pprl.service.generator.selection.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class IdGenerator {

	private final Random rnd;

	/**
	 * TODO: Currently it is necessary to generate new local (source) ids for each record.
	 *
	 * The original dataset contain duplicates with the same id at the person-object level.
	 * However, the id on the higher "duplicate level" seems to be unique.
	 *
	 * One example is the cluster CW963791.
	 * Here multiple duplicates share the person-level id bf84967d2dbd2e89f091f02fbb353cc7.
	 *
	 */
	final Set<String> localIds = new HashSet<>();

	public IdGenerator(Long seed) {
		this.rnd = new Random(seed);
	}

	public String getNextId() {
		String id = null;

		while(id == null || localIds.contains(id)) {
			id = RandomStringUtils.random(16, 0, 0, true, true, null, rnd);
			// TODO: What is the purpose of the set if it is never used? Probably not used because it is to slow?
//			localIds.add(id);
		}

		return id;
	}

	public String getRnd() {
		int length = 32;
		boolean useLetters = true;
		boolean useNumbers = true;
		return RandomStringUtils.random(length, 0, 0, useLetters, useNumbers, null, rnd);
	}
}
