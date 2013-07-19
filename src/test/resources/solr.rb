require 'rsolr'

solr = RSolr.connect :url => "http://localhost:8983/solr/eri"
solr.ping(1)