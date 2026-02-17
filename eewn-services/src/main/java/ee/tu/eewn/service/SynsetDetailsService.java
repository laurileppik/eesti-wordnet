package ee.tu.eewn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;

@Service
@RequiredArgsConstructor
public class SynsetDetailsService {
    @PersistenceContext
    private EntityManager em;

    public String getSynsetDetails(Integer id) {
        String sql =
"""
SELECT jsonb_pretty(
    jsonb_build_object(
        'id', orig_syn.id,
        'label', orig_syn.label,
        'synsetType', orig_syn.synset_type,
        'status', orig_syn.status,
        'comment', orig_syn.comment,
        'definitions',
        COALESCE(
            (
                SELECT jsonb_agg(orig_syn_def.text)
                FROM wnwb_definition orig_syn_def
                WHERE orig_syn_def.synset_id = orig_syn.id
                  AND orig_syn_def.is_deleted IS FALSE
            ), '[]'::jsonb
        ),
        'senses',
        COALESCE(
            (
                SELECT jsonb_agg(
                    jsonb_build_object(
                        'id', orig_sense.id,
                        'lemma', orig_lex_entry.lemma,
                        'partOfSpeech', orig_lex_entry.part_of_speech,
                        'status', orig_sense.status,
                        'comment', orig_sense.comment,
                        'label', orig_sense.label,
                        'examples', COALESCE((
                            SELECT jsonb_agg(sense_example.text)
                            FROM wnwb_senseexample sense_example
                            WHERE sense_example.sense_id = orig_sense.id
                              AND sense_example.is_deleted IS FALSE
                        ), '[]'::jsonb)
                    )
                )
                FROM wnwb_sense orig_sense
                    JOIN wnwb_lexicalentry orig_lex_entry
                        ON orig_lex_entry.id = orig_sense.lexical_entry_id
                        AND orig_lex_entry.is_deleted IS FALSE
                WHERE orig_sense.synset_id = orig_syn.id
                  AND orig_sense.is_deleted IS FALSE
            ), '[]'::jsonb
        ),
        'relations',
        COALESCE(
            (
                SELECT jsonb_agg(
                    jsonb_build_object(
                        'type', syn_rel_type.name,
                        'relatedSynsetId', rel_syn.id,
                        'relatedLabel', rel_syn.label,
                        'relevantWords',
                        COALESCE(
                            (
                                SELECT jsonb_agg(rel_sense.label)
                                FROM wnwb_sense rel_sense
                                    JOIN wnwb_lexicalentry rel_lex_entry
                                        ON rel_lex_entry.id = rel_sense.lexical_entry_id
                                        AND rel_lex_entry.is_deleted IS FALSE
                                WHERE rel_sense.synset_id = rel_syn.id
                                  AND rel_sense.is_deleted IS FALSE
                            ), '[]'::jsonb
                        )
                    )
                )
                FROM wnwb_synsetrelation syn_rel
                    JOIN wnwb_synsetrelationtype syn_rel_type
                        ON syn_rel_type.id = syn_rel.rel_type_id
                        AND syn_rel_type.is_deleted IS FALSE
                    JOIN wnwb_synset rel_syn
                        ON rel_syn.id = syn_rel.b_synset_id
                WHERE syn_rel.a_synset_id = orig_syn.id
                  AND syn_rel.is_deleted IS FALSE
            ), '[]'::jsonb
        ),
        'tags',
        COALESCE(
            (
                SELECT jsonb_agg(orig_tag.value)
                FROM wnwb_synsettag orig_syn_tag
                    JOIN wnwb_tag orig_tag
                        ON orig_tag.id = orig_syn_tag.tag_id
                        AND orig_tag.is_deleted IS FALSE
                WHERE orig_syn_tag.synset_id = orig_syn.id
                  AND orig_syn_tag.is_deleted IS FALSE
            ), '[]'::jsonb
        ),
        'externalReferences',
        COALESCE(
            (
                SELECT jsonb_agg(
                    jsonb_build_object(
                        'systemName', ext_sys.name,
                        'relationType', ext_rel_type.name,
                        'reference', ext_ref.reference,
                        'words',
                        COALESCE(
                            (
                                SELECT jsonb_agg(
                                    ext_sense.label
                                )
                                FROM wnwb_synset ext_syn
                                    JOIN wnwb_sense ext_sense
                                        ON ext_sense.synset_id = ext_syn.id
                                        AND ext_sense.is_deleted IS FALSE
                                    JOIN wnwb_lexicalentry ext_lex_entry
                                        ON ext_lex_entry.id = ext_sense.lexical_entry_id
                                        AND ext_lex_entry.is_deleted IS FALSE
                                WHERE ext_syn.label = ext_ref.reference
                            ), '[]'::jsonb
                        ),
                        'definition',
                        (
                            SELECT ext_def.text
                            FROM wnwb_synset ext_syn
                                JOIN wnwb_definition ext_def
                                    ON ext_def.synset_id = ext_syn.id
                                    AND ext_def.is_deleted IS FALSE
                            WHERE ext_syn.label = ext_ref.reference
                            LIMIT 1
                        )
                    )
                )
                FROM wnwb_externalref ext_ref
                    JOIN wnwb_externalsystem ext_sys
                        ON ext_sys.id = ext_ref.sys_id_id
                    JOIN wnwb_externalrelationtype ext_rel_type
                        ON ext_rel_type.id = ext_ref.rel_type_id
                WHERE ext_ref.synset_id = orig_syn.id
            ), '[]'::jsonb
        )
    )
) AS pretty_json
FROM wnwb_synset orig_syn
WHERE orig_syn.id = ?1
  AND orig_syn.is_deleted IS FALSE;
""";
        Query q = em.createNativeQuery(sql);
        q.setParameter(1, id);
        Object res;
        try {
            res = q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        if (res == null) return null;
        return res.toString();
    }
}
