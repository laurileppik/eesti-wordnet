package ee.tu.eewn.service;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.jsonbArrayAgg;
import static org.jooq.impl.DSL.jsonbObject;
import static org.jooq.impl.DSL.key;
import static org.jooq.impl.DSL.select;

import lombok.RequiredArgsConstructor;
import nu.studer.sample.tables.WnwbDefinition;
import nu.studer.sample.tables.WnwbExternalrelationtype;
import nu.studer.sample.tables.WnwbExternalsystem;
import nu.studer.sample.tables.WnwbLexicalentry;

import org.jooq.Field;
import org.jooq.JSONB;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalReferenceService {
    public Field<JSONB> getExternalReferences(nu.studer.sample.tables.WnwbSense origSense, nu.studer.sample.tables.WnwbSynset extSyn, WnwbLexicalentry origLex, nu.studer.sample.tables.WnwbExternalref extRef,
                                                     Field<JSONB> emptyJsonArray, WnwbDefinition extDef, WnwbExternalsystem extSys,
                                                     WnwbExternalrelationtype extRelType, nu.studer.sample.tables.WnwbSynset relSyn) {
        Field<JSONB> extWords =
            coalesce(
                field(
                    select(jsonbArrayAgg(origSense.LABEL))
                        .from(extSyn)
                        .join(origSense)
                        .on(origSense.SYNSET_ID.eq(extSyn.ID))
                        .and(origSense.IS_DELETED.isFalse())
                        .join(origLex)
                        .on(origLex.ID.eq(origSense.LEXICAL_ENTRY_ID))
                        .and(origLex.IS_DELETED.isFalse())
                        .where(extSyn.LABEL.eq(extRef.REFERENCE))
                ), emptyJsonArray
            );

        Field<String> extDefinition =
            field(
                select(extDef.TEXT)
                    .from(extSyn)
                    .join(extDef)
                    .on(extDef.SYNSET_ID.eq(extSyn.ID))
                    .and(extDef.IS_DELETED.isFalse())
                    .where(extSyn.LABEL.eq(extRef.REFERENCE))
                    .limit(1)
            );

        return coalesce(
            field(
                select(jsonbArrayAgg(
                    jsonbObject(
                        key("systemName").value(extSys.NAME),
                        key("relationType").value(extRelType.NAME),
                        key("reference").value(extRef.REFERENCE),
                        key("words").value(extWords),
                        key("definition").value(extDefinition)
                    )
                ))
                    .from(extRef)
                    .join(extSys)
                    .on(extSys.ID.eq(extRef.SYS_ID_ID))
                    .join(extRelType)
                    .on(extRelType.ID.eq(extRef.REL_TYPE_ID))
                    .where(extRef.SYNSET_ID.eq(relSyn.ID))
            ),
            emptyJsonArray
        );
    }
}
