# Chart changelog

#### Commit #1 (23rd Jan)
Decided to use a chart library (XChart) to enable the use of good looking graphs with the ability to be saved.

Changes made to gui.java file:
* Added new hPanel and JPanel on the gui.java file
* Action listener added to gui.java which calls update function on tab change to keep the ChartPanel updated.

Also created new ChartPanel class which extends from JPanel. It has checkboxes for each stock and these checkboxes should change if the stock is shown on the graph.