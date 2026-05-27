package de.unileipzig.dbs.pprl.service.generator.selection.model.common;

import lombok.*;

import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenericRawRecord {

	public static final String ID_NAME = "voterId";

	private String voterId;

	private DateInfo snapShot;

	@Singular
	private Map<String, String> attributes;
}
