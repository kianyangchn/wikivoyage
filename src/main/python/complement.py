# -*- coding: utf-8 -*-
# @Author: kianyangchn
# @Date:   2018-10-06 16:31:20
# @Last Modified by:   kianyangchn
# @Last Modified time: 2018-10-06 21:15:00

import sys
import codecs
import nltk
import string
import ujson

stops = set(nltk.corpus.stopwords.words("english"))


def clean_text(text):
    text = text.lower()
    transtable = text.maketrans('', '', string.punctuation)
    tokens = nltk.word_tokenize(text)
    tokens = [token.translate(transtable) for token in tokens]
    tokens = list(filter(lambda token: len(token) >
                         0 and token not in stops, tokens))
    return ' '.join(tokens)


if __name__ == '__main__':
    listings_file = sys.argv[1]
    content_file = sys.argv[2]
    print('load listings file')
    lines = list(codecs.open(listings_file, 'r', 'utf-8').readlines())
    print('parse json')
    infos = [ujson.loads(line.strip()) for line in lines]
    print('build corpus')
    corpus = [' '.join([info['pageTitle'], info['name'],
                        info['content']]) for info in infos]
    print('clean text')
    corpus = [clean_text(text) for text in corpus]
    print('write')
    with codecs.open(content_file, 'w', 'utf-8') as cf:
        for i in range(len(infos)):
            text = corpus[i]
            content_len = str(len(infos[i]['content'].split(' ')))
            infos[i]['text'] = text
            infos[i]['content_len'] = content_len
            cf.write('%s\n' % ujson.dumps(infos[i]))
