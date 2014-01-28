#!/usr/bin/python

import wave
import array
import sys

def fopen(path):
    return wave.open(path, 'r')

# src: binary input from clip
def compute_acf(src):
    N = int(len(src))
    res = list(range(N))
    k = 0
    
    for k in range(N):
        n = 0
        res[k] = 0
        while (n < N - k):
            if (src[n] == 1) & (src[n + k] == 1):
                res[k] += 1
            n += 1
        res[k] /= N - k
    return res

def cl(src):
    acf_start = 16
    acf_end = 400
    mi = 40000
    ma = -40000
    
    # find min and max
    i = acf_start
    while (i < acf_end):
        if (src[i] < mi):
            mi = src[i];
        if (src[i] > ma):
            ma = src[i];
        i += 1
    
    level = 0.43 * (ma + mi)
    
    # print('max={0} min={1} level={2}'.format(ma,mi,level))
    res = list(range(acf_end - acf_start))
    
    i = acf_start
    while (i < acf_end):
        if (src[i] < level):
            res[i - acf_start] = 1
        else:
            res[i - acf_start] = 0
        i += 1
        
    return res

def compute_amdf(snd):
    N = int(len(snd))
    res = list(range(N))
    m = 0
    for m in range(N):
        n = 0
        res[m] = 0
        while (n < N - m):
            res[m] += abs(snd[n + m] - snd[n])
            # print('m={0} n={1} sum={2}'.format(m,n,res[m]))
            n += 1
        res[m] /= N - m
    return res
    
def main():
    f = fopen(sys.argv[1])
    data = array.array('h', f.readframes(f.getnframes()))
    #~ for s in data:
        #~ print('{0}, '.format(s),end='')
    #~ print(f.getnframes())
    amdfed = compute_amdf(data)
    clipped = cl(amdfed)
    acfed = compute_acf(clipped)
    
    orig = open('orig', 'w')
    amdf = open('amdf', 'w')
    clip = open('clip', 'w')
    acf = open('acf', 'w')
    
    x = 0
    orig.write('# X  Y\n')
    for y in data:
        orig.write('  {0}  {1}\n'.format(x,y))
        x += 1
    
    x = 0
    #amdf.write('{0}\n'.format(len(amdfed)))
    amdf.write('# X  Y\n')
    for y in amdfed:
        amdf.write('  {0}  {1}\n'.format(x,y))
        x += 1

    x = 0
    clip.write('# X  Y\n')
    for y in clipped:
        clip.write('  {0}  {1}\n'.format(x,y))
        x += 1

    x = 0
    acf.write('# X  Y\n')
    for y in acfed:
        acf.write('  {0}  {1}\n'.format(x,y))
        x += 1

    orig.close()
    amdf.close()
    clip.close()
    acf.close()

    #~ index = 1
    #~ i = 2
    #~ while i < len(res):
        #~ if res[i] > res[index]:
            #~ index = i
        #~ i += 1
    #~ 
    #~ print("max je na {0} tj. {1} Hz".format(index, f.getframerate()/index))
    
main()
