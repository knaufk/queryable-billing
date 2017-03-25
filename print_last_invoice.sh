ls -t build/invoices/ | head -1 | xargs -i sh -c 'tail build/invoices/{}/part*' | grep EUR
