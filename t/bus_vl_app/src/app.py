﻿#!/usr/bin/python
# -*- coding: utf-8 -*-

# Copyright 2008 David Noble <dnoble@dnoble.org>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import webapp2
import logging, traceback
import os
import re
import cgi
import base64
import urllib
import datetime
import time


#from google.appengine.api import users
from google.appengine.ext import ndb
#from google.appengine.ext import webapp
#from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.api import memcache

class Comment(ndb.Model):
  idbus = ndb.StringProperty()
  title = ndb.StringProperty()
  body = ndb.StringProperty()
  route = ndb.StringProperty()
  loc = ndb.StringProperty()#encoding='utf_8')
  text = ndb.StringProperty(indexed=False)#TextProperty
  date = ndb.DateTimeProperty(auto_now_add=True)
  #def formatted_date(self):
  #  return self.date.strftime('%d %b %Y %H:%M:%S')

class AddComment(webapp2.RequestHandler):
  def post(self):
    dat = self.request.get('myHttpData')
    #d = base64.urlsafe_b64decode(dat)
    d = dat.split('|');
    if (len(d)>4):
        idbus      = ""+d[0]
        title   = d[1]
        body    = d[2]
        route   = d[3]
        loc     = d[4]
        text    = d[5]
        com = Comment(text=text, idbus=idbus,title=title, body=body, route=route, loc=loc)
        key1 = com.put()  
        memcache.flush_all()        
  def get(self):
    self.response.out.write('')

def get_comments(last_id):
    status = memcache.get('%s:status' % last_id)
    if status is not None:
        com_str = memcache.get('%s:coment' % last_id)
        if com_str is not None:
            return com_str,status
        else:
            com_str,is_empty = get_comments_db(last_id)
            if not memcache.add('%s:coment' % last_id, com_str, 0): #add(key, value, time=0, min_compress_len=0, namespace=None)
                logging.error('Memcache set failed.')
            if not memcache.add('%s:status' % last_id, is_empty, 0):
                logging.error('Memcache set failed1.')
            return com_str,is_empty
    else:
            com_str,is_empty = get_comments_db(last_id)
            if not memcache.add('%s:coment' % last_id, com_str, 0): #add(key, value, time=0, min_compress_len=0, namespace=None)
                logging.error('Memcache set failed.')
            if not memcache.add('%s:status' % last_id, is_empty, 0):
                logging.error('Memcache set failed1.')
            return com_str,is_empty

def get_comments_db(last_id):
    if last_id=="null":
        last_id="0"
    else:
      if last_id is None:
        last_id="0"
      else:
        if last_id=="":
          last_id="0"
    long_date=long(last_id)
    last_date=datetime.datetime.fromtimestamp(long_date/1000000)
    #last_date=last_date+(long(last_id)%1000000)/1000000
    com_query = Comment.query(Comment.date>last_date).order(-Comment.date)
    com = com_query.fetch(20)

    is_empty=True
    s = '{last:11,data:{comments:['
    for item in com:
      full_date=long(item.date.strftime("%s%f"))
      if full_date>long_date:
        is_empty=False
        s = s + '['
        s = s + '"'+str(item.key.id())+'",'
        s = s + '"'+item.idbus+'",'
        s = s + '"'+item.title+'",'
        s = s + '"'+item.body+'",'
        s = s + '"'+item.route+'",'
        s = s + '"'+item.loc+'",'
        s = s + '"'+item.date.strftime("%s%f")+'",' #141 284 8237 900100
        #s = s + '"'+item.date.strftime("%s")+'",' #141 283 1358
        #s = s + '"'+item.date.strftime("%y/%m/%d")+'",'              #2014/09/05
        s = s + '"'+item.text+'"'
        #s = s + '"'+urllib.quote('абвгд').encode('utf-8')+'"'
        s = s + '],'
    s=s+'[]]}}'
    return s,is_empty

class GetComment(webapp2.RequestHandler):
  def get(self):
    last = self.request.get('last')
    s,is_empty=get_comments(last);
    if (is_empty):
        self.error(304) #no need read
    self.response.out.write(s)

class PageRenderer(webapp2.RequestHandler):
  def get(self, url):
    self.error(404)

def unicode_to_string(s):
    if s is not None:
        if isinstance(s, unicode):
            s = s.encode('utf-8')
    return s
    
application = webapp2.WSGIApplication([
                ('/add/comment/', AddComment),
                ('/get/comment/', GetComment),
                #('/edit/pages/(.*)', PageEditor),
                #('(/.*)', PageRenderer),
              ])
