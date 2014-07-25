import csv
import os

from models import AlignmentFile
from matching import MaxWeightedMatching

class BaseAligner(object):

    @staticmethod
    def select_aligner(alignment_method, alignment_files, options):
        print
        if alignment_method.lower() == 'mw':
            if options.exact_match:
                print "Alignment method = exact max weighted alignment"
            else:
                print "Alignment method = approximate max weighted alignment"                
            aligner = MaxWeightedAligner(alignment_files, options)
        else:
            print "Alignment method " + alignment_method + " not found. Terminating."
            exit(1)
        return aligner

    def __init__(self, alignment_files, options):
        self.alignment_files = alignment_files
        self.options = options
        self.matched_results = AlignmentFile("")

    def do_alignment(self):
        raise NotImplementedError("Not implemented")

    def save_results(self, output_path):
        '''Writes the matching results'''

        # if file doesn't exist, create it
        if not os.path.exists(os.path.dirname(output_path)):
            os.makedirs(os.path.dirname(output_path))        
        
        # write the result out in sima format, separated by tab
        # the columns are:
        # 1. alignment group id, index starts from 0
        # 2. originating file name
        # 3. position of feature in data, index starts from 0
        # 4. mass of feature
        # 5. rt of feature
        with open(output_path, 'wb') as f:
            writer = csv.writer(f, delimiter='\t', quoting=csv.QUOTE_NONE)            
            for row in self.matched_results.rows:
                for feature in row.features:
                    out = [row.row_id, feature.parent_file.filename, feature.peak_id, feature.mass, feature.rt]
                    writer.writerow(out)        
        
        print
        print 'Output written to', output_path
        
class MaxWeightedAligner(BaseAligner):
        
    def __init__(self, alignment_files, options):
        super(MaxWeightedAligner, self).__init__(alignment_files, options)        

    def do_alignment(self):

        num_files = len(self.alignment_files)        
        for i in range(num_files):
            alignment_file = self.alignment_files[i]
            # match the files
            print ("\nProcessing " + alignment_file.filename + " [" + str(i+1) + "/" + str(num_files) + "]")
            matcher = MaxWeightedMatching(self.matched_results, alignment_file, self.options)
            self.matched_results = matcher.do_matching()
        
        return self.matched_results