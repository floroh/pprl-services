package de.unileipzig.dbs.pprl.core.common.model.impl;

import de.unileipzig.dbs.pprl.core.common.model.api.Attribute;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordCluster;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordId;
import de.unileipzig.dbs.pprl.core.common.model.api.RecordPair;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagProvider;
import de.unileipzig.dbs.pprl.core.common.monitoring.TagTable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class RecordWithTags implements Record, TagProvider {

	private final Record record;

	private final TagTable tagTable;

	public RecordWithTags(Record record, TagTable tagTable) {
		this.record = record;
		this.tagTable = tagTable;
	}
	public RecordWithTags(Record record) {
		this.record = record;
		this.tagTable = new TagTable();
	}

	@Override
	public TagTable provideTagTable() {
		return tagTable;
	}

	public TagTable getTagTable() {
		return tagTable;
	}

	@Override
	public RecordId getId() {
		return record.getId();
	}

	@Override
	public void setId(RecordId recordId) {
		record.setId(recordId);
	}

	@Override
	public Optional<Attribute> getAttribute(String name) {
		return record.getAttribute(name);
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return record.getAttributes();
	}

	@Override
	public Record setAttribute(String name, Attribute attribute) {
		return record.setAttribute(name, attribute);
	}

	@Override
	public Record removeAttribute(String name) {
		return record.removeAttribute(name);
	}

	@Override
	public Record duplicate() {
		return new RecordWithTags(
			record.duplicate(),
			tagTable.duplicate()
		);
	}

	@Override
	public RecordPair getPair(Record other) {
		return record.getPair(other);
	}

	@Override
	public RecordCluster getCluster() {
		return record.getCluster();
	}

	@Override
	public int getNumberOfAttributes() {
		return record.getNumberOfAttributes();
	}

	@Override
	public Set<String> getAttributeNames() {
		return record.getAttributeNames();
	}
}
