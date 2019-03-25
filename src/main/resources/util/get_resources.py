'''
Python Script that writes out all the registered resources from a given Search Service
onto an output file. The intent is that this output file can then be fed into the Harvest Tool
to populate resource_ref.* fields.

@author: mcayanan
'''
from __future__ import print_function

from urllib2 import *
import json
import argparse
import logging
import traceback
import sys


SEARCH_URL = 'https://pds.nasa.gov/services/search'
OUTPUT_FILE_NAME = 'registered_resources.json'
ENDPOINT = "search"
QUERY = "q=product_class:Product_Context%20AND%20data_class:Resource&wt=json"

# Set up logging
logging.basicConfig(level=logging.INFO)
LOGGER = logging.getLogger(__name__)

def get_parser():
    '''
    Get a parser for this application
    @return: parser to for this application
    '''
    parser = argparse.ArgumentParser(description="Gets the latest Resource Products from a given Registry")
    parser.add_argument("--search_url", nargs=1, required=False, help="Specify the Search Service URL to look for Resource Products. Default is to go to {}.".format(SEARCH_URL))
    parser.add_argument("--output", nargs=1, required=False, help="Specify an output file to write the results. Default is to write to a file named '{}'.".format(OUTPUT_FILE_NAME))
    
    return parser

def get_resource_products(search_url, output_file):
    query_url = "{}/{}/?{}".format(search_url,ENDPOINT,QUERY)
    connection = urlopen(query_url)
    response = json.load(connection)
    total_results = response['response']['numFound']
    LOGGER.info("{} documents found. Getting the resources...".format(total_results))

    #f = open(output_file,"w+")

    #
    counter = 0
    registered_resources = {}
    while len(response['response']['docs']) != 0:
        resource = None
        for document in response['response']['docs']:
            resource_info = {}
            resource_info['resource_name'] = document['resource_name'][0]
            resource_info['resource_url'] = document['resource_url'][0]
            registered_resources[document['identifier']] = resource_info
            counter = counter + 1
        LOGGER.info("Processed {} documents".format(counter))
        connection = urlopen("{}&start={}&rows=100".format(query_url,counter))
        response = json.load(connection)
        
    with open(output_file, "w+") as f:
        json.dump(registered_resources, f, indent=2, sort_keys=True)
    

def main():
    '''
    Main entry point
    '''
    search_url = SEARCH_URL
    output_file = OUTPUT_FILE_NAME
    args = get_parser().parse_args()
    
    if args.search_url != None:
        search_url = args.search_url[0]
        
    if args.output != None:
        output_file = args.output[0]
            
    get_resource_products(search_url, output_file)
    LOGGER.info("{} has been created.".format(output_file))
    
if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        LOGGER.error(traceback.format_exc())
        sys.exit(1)