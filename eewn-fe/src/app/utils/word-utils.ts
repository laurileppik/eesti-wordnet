export function parseSenseLabel(label: string | undefined): { lemma: string, subscript: string } {
  if (!label) return { lemma: '', subscript: '' };
  const match = new RegExp(/^(.*?)(_\d+\(.*\))$/).exec(label);
  if (match) {
    return { lemma: match[1], subscript: match[2].slice(1) };
  }
  return { lemma: label, subscript: '' };
}

export function formatWords(words: (string | undefined)[]): { lemma: string, subscript: string }[] {
  if (!Array.isArray(words)) return [];
  return words.map(word => parseSenseLabel(word));
}
