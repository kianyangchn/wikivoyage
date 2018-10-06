# -*- coding: utf-8 -*-
# @Author: kianyangchn
# @Date:   2018-10-06 17:28:43
# @Last Modified by:   kianyangchn
# @Last Modified time: 2018-10-06 17:31:00


import sys
import codecs
import ujson

if __name__ == '__main__':
    listings_file = sys.argv[1]
    vector_file = sys.argv[2]
    result_file = sys.argv[3]
    print('load listings file')
    lines = list(codecs.open(listings_file, 'r', 'utf-8').readlines())
    print('parse json')
    infos = [ujson.loads(line.strip()) for line in lines]
    print('load vectors')
    lines = list(codecs.open(vector_file, 'r', 'utf-8').readlines())
    print('write')
    with codecs.open(result_file, 'w', 'utf-8') as rf:
        for i in range(len(infos)):
            vector = lines[i].strip()
            infos[i]['vector'] = vector
            rf.write('%s\n' % ujson.dumps(infos[i]))
