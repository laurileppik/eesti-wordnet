package ee.tu.eewn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jooq.*;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.*;
import static nu.studer.sample.tables.WnwbSynset.WNWB_SYNSET;
import static nu.studer.sample.tables.WnwbDefinition.WNWB_DEFINITION;
import static nu.studer.sample.tables.WnwbSense.WNWB_SENSE;
import static nu.studer.sample.tables.WnwbLexicalentry.WNWB_LEXICALENTRY;
import static nu.studer.sample.tables.WnwbExternalref.WNWB_EXTERNALREF;
import static nu.studer.sample.tables.WnwbExternalsystem.WNWB_EXTERNALSYSTEM;
import static nu.studer.sample.tables.WnwbExternalrelationtype.WNWB_EXTERNALRELATIONTYPE;
import static nu.studer.sample.tables.WnwbSynsetrelationtype.WNWB_SYNSETRELATIONTYPE;
import static nu.studer.sample.tables.WnwbSynsetrelation.WNWB_SYNSETRELATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class SynsetRelationService {
    private final DSLContext dsl;
    private final ExternalReferenceService externalReferenceService;

    public String getRelationsJsonJooq(Integer synsetId) {
        var origSyn = WNWB_SYNSET.as("orig_syn");
        var origSynDef = WNWB_DEFINITION.as("orig_syn_def");
        var origSense = WNWB_SENSE.as("orig_sense");
        var origLex = WNWB_LEXICALENTRY.as("orig_lex_entry");

        var synRel = WNWB_SYNSETRELATION.as("syn_rel");
        var synRelType = WNWB_SYNSETRELATIONTYPE.as("syn_rel_type");
        var relSyn = WNWB_SYNSET.as("rel_syn");
        var relSense = WNWB_SENSE.as("rel_sense");
        var relLex = WNWB_LEXICALENTRY.as("rel_lex_entry");

        var extRef = WNWB_EXTERNALREF.as("ext_ref");
        var extSys = WNWB_EXTERNALSYSTEM.as("ext_sys");
        var extRelType = WNWB_EXTERNALRELATIONTYPE.as("ext_rel_type");
        var extSyn = WNWB_SYNSET.as("ext_syn");
        var extDef = WNWB_DEFINITION.as("ext_def");
        Field<JSONB> emptyJsonArray = cast(inline("[]"), JSONB.class);

        Field<JSONB> relevantWords =
            coalesce(
                field(
                    select(jsonbArrayAgg(relSense.LABEL))
                        .from(relSense)
                        .join(relLex)
                        .on(relLex.ID.eq(relSense.LEXICAL_ENTRY_ID))
                        .and(relLex.IS_DELETED.isFalse())
                        .where(relSense.SYNSET_ID.eq(relSyn.ID))
                        .and(relSense.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<String> definition =
            field(
                select(origSynDef.TEXT)
                    .from(origSynDef)
                    .where(origSynDef.SYNSET_ID.eq(relSyn.ID))
                    .and(origSynDef.IS_DELETED.isFalse())
                    .orderBy(origSynDef.IS_PRIMARY.desc())
                    .limit(1)
            );

        Field<JSONB> externalReferences = externalReferenceService.getExternalReferences(origSense, extSyn, origLex, extRef, emptyJsonArray, extDef, extSys, extRelType, relSyn);

        Field<JSONB> relationObject =
            jsonbObject(
                key("synsetId").value(relSyn.ID),
                key("definition").value(definition),
                key("relevantWords").value(relevantWords),
                key("externalReferences").value(externalReferences)
            );

        Field<JSONB> relations =
            coalesce(
                field(
                    select(jsonbObjectAgg(
                        synRelType.NAME,
                        field(
                            select(jsonbArrayAgg(relationObject))
                                .from(synRel)
                                .join(relSyn)
                                .on(relSyn.ID.eq(synRel.B_SYNSET_ID))
                                .where(synRel.A_SYNSET_ID.eq(origSyn.ID))
                                .and(synRel.REL_TYPE_ID.eq(synRelType.ID))
                                .and(synRel.IS_DELETED.isFalse())
                        )
                    ))
                        .from(synRelType)
                        .whereExists(
                            selectOne()
                                .from(synRel)
                                .where(synRel.A_SYNSET_ID.eq(origSyn.ID))
                                .and(synRel.REL_TYPE_ID.eq(synRelType.ID))
                                .and(synRel.IS_DELETED.isFalse())
                        )
                ),
                emptyJsonArray
            );

        return dsl.select(relations)
                  .from(origSyn)
                  .where(origSyn.ID.eq(synsetId))
                  .and(origSyn.IS_DELETED.isFalse())
                  .fetchOneInto(String.class);
    }
}
