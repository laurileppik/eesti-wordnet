package ee.tu.eewn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import static org.jooq.impl.DSL.*;
import static nu.studer.sample.tables.WnwbSynset.WNWB_SYNSET;
import static nu.studer.sample.tables.WnwbDefinition.WNWB_DEFINITION;
import static nu.studer.sample.tables.WnwbSense.WNWB_SENSE;
import static nu.studer.sample.tables.WnwbLexicalentry.WNWB_LEXICALENTRY;
import static nu.studer.sample.tables.WnwbSenseexample.WNWB_SENSEEXAMPLE;
import static nu.studer.sample.tables.WnwbSynsetrelation.WNWB_SYNSETRELATION;
import static nu.studer.sample.tables.WnwbSynsetrelationtype.WNWB_SYNSETRELATIONTYPE;
import static nu.studer.sample.tables.WnwbSynsettag.WNWB_SYNSETTAG;
import static nu.studer.sample.tables.WnwbTag.WNWB_TAG;
import static nu.studer.sample.tables.WnwbExternalref.WNWB_EXTERNALREF;
import static nu.studer.sample.tables.WnwbExternalsystem.WNWB_EXTERNALSYSTEM;
import static nu.studer.sample.tables.WnwbExternalrelationtype.WNWB_EXTERNALRELATIONTYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class SynsetDetailsService {
    private final DSLContext dsl;
    private final ExternalReferenceService externalReferenceService;

    public String getSynsetDetails(Integer id) {
        var origSyn = WNWB_SYNSET.as("orig_syn");
        var origSynDef = WNWB_DEFINITION.as("orig_syn_def");
        var origSense = WNWB_SENSE.as("orig_sense");
        var origLex = WNWB_LEXICALENTRY.as("orig_lex_entry");
        var origSynTag = WNWB_SYNSETTAG.as("orig_syn_tag");
        var origTag = WNWB_TAG.as("orig_tag");
        var senseExample = WNWB_SENSEEXAMPLE.as("sense_example");

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

        Field<JSONB> definitions =
            coalesce(
                field(
                    select(jsonbArrayAgg(origSynDef.TEXT))
                        .from(origSynDef)
                        .where(origSynDef.SYNSET_ID.eq(origSyn.ID))
                        .and(origSynDef.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> examples =
            coalesce(
                field(
                    select(jsonbArrayAgg(senseExample.TEXT))
                        .from(senseExample)
                        .where(senseExample.SENSE_ID.eq(origSense.ID))
                        .and(senseExample.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> senses =
            coalesce(
                field(
                    select(jsonbArrayAgg(
                        jsonbObject(
                            key("id").value(origSense.ID),
                            key("lemma").value(origLex.LEMMA),
                            key("partOfSpeech").value(origLex.PART_OF_SPEECH),
                            key("status").value(origSense.STATUS),
                            key("comment").value(origSense.COMMENT),
                            key("label").value(origSense.LABEL),
                            key("examples").value(examples)
                        )
                    ))
                        .from(origSense)
                        .join(origLex)
                        .on(origLex.ID.eq(origSense.LEXICAL_ENTRY_ID))
                        .and(origLex.IS_DELETED.isFalse())
                        .where(origSense.SYNSET_ID.eq(origSyn.ID))
                        .and(origSense.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

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

        Field<JSONB> relations =
            coalesce(
                field(
                    select(jsonbArrayAgg(
                        jsonbObject(
                            key("type").value(synRelType.NAME),
                            key("relatedSynsetId").value(relSyn.ID),
                            key("relatedLabel").value(relSyn.LABEL),
                            key("relevantWords").value(relevantWords)
                        )
                    ))
                        .from(synRel)
                        .join(synRelType)
                        .on(synRelType.ID.eq(synRel.REL_TYPE_ID))
                        .and(synRelType.IS_DELETED.isFalse())
                        .join(relSyn)
                        .on(relSyn.ID.eq(synRel.B_SYNSET_ID))
                        .where(synRel.A_SYNSET_ID.eq(origSyn.ID))
                        .and(synRel.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> tags =
            coalesce(
                field(
                    select(jsonbArrayAgg(origTag.VALUE))
                        .from(origSynTag)
                        .join(origTag)
                        .on(origTag.ID.eq(origSynTag.TAG_ID))
                        .and(origTag.IS_DELETED.isFalse())
                        .where(origSynTag.SYNSET_ID.eq(origSyn.ID))
                        .and(origSynTag.IS_DELETED.isFalse())
                ),
                emptyJsonArray
            );

        Field<JSONB> externalReferences = externalReferenceService.getExternalReferences(origSense, extSyn, origLex, extRef, emptyJsonArray, extDef, extSys, extRelType, origSyn);

        Field<JSONB> json =
            jsonbObject(
                key("id").value(origSyn.ID),
                key("label").value(origSyn.LABEL),
                key("synsetType").value(origSyn.SYNSET_TYPE),
                key("status").value(origSyn.STATUS),
                key("comment").value(origSyn.COMMENT),
                key("definitions").value(definitions),
                key("senses").value(senses),
                key("relations").value(relations),
                key("tags").value(tags),
                key("externalReferences").value(externalReferences)
            );

        Field<JSONB> pretty =
            DSL.function("jsonb_pretty", JSONB.class, json);
        SelectConditionStep<Record1<JSONB>> query = dsl
            .select(pretty)
            .from(origSyn)
            .where(origSyn.ID.eq(id))
            .and(origSyn.IS_DELETED.isFalse());
        log.info("[jOOQ SQL] {}", query.getSQL());
        log.info("[jOOQ INLINED SQL] {}", query.getSQL(ParamType.INLINED));
        return query.fetchOneInto(String.class);
    }
}
