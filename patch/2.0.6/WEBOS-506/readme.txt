Summary
	* Issue title: Direct opening of portlet to the right path 
	* CCP Issue:  CCP-960 
	* Product Jira Issue: WEBOS-506.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* The opening direct of portlet to the right path - When the webOS will be integrated into the platform, the customer wants that the portlet File Explorer and forum be automatically opened after the log in by using a suitable link (under the webOS) 

Fix description

Problem analysis
	* That type of direct link should have portlet id on the url, then when user makes request with that url, portal can parse url and show corresponding application

How is the problem fixed?
	* Use navigation controller feature provided in gatein, it helps user get the url
    	* Navigation controller engine is improved in EXOGTN-1171

Tests to perform

Reproduction test
	* Login as John
    	* Go to the Desktop application
    	* Add an application: "Application Registry" portlet
    	* Click "Application Registry" application on the Dockbar, the application window will be shown on the Desktop.
    	* Logout of WebOS page
    	* Login from the link http://localhost:8080/portal/u/john/classicWebosPage
    		=> the "Application Registry" application doesn't show on the Desktop.

Tests performed at DevLevel
	* c.f above

Tests performed at Support Level
	* c.f above

Tests performed at QA
	*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* No need

Changes in Selenium scripts 
	* No need

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

Configuration changes

Configuration changes:
	* Need to change controller.xml and config plugin for URLFactoryService

Will previous configuration continue to work?
	* No

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change: No
	* Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	*

QA Feedbacks
	*
