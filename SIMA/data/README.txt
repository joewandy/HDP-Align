SIMA - Simultaneous Multiple Alignment of LC/MS Data
=====================================================

These folders contain peak lists ready to use with the SIMA command line tool. The fictive instrument characteristics are drt = 0.5 and dmz = 0.5.

For a first try, run the SIMA tool like this:
SIMA -R 0.5 -M 0.5 -i [/path/to/data]

/data_simple contains similar peak lists. The peak lists are created from one 'perfect' peak list by adding noise to rt or mz or by modulating rt with a sine/cosine signal. Using the parameters above, every peak should have one parter in every other peak list. The Master Time Scale should be the function f(t) = (t,t,t,t)'.

/data_charge contains peak lists that match perfectly in rt and mz. The charge state of the entries is set to 1 or 2 randomly. If only peaks with equal charge state are merged (-Z 0, default), there should be correspondence groups of size 2,3 and 4 and sole peaks. If a more tolerant charge normalization is chosen (-Z n, n very large), every peak should have a match in every other peak list.

