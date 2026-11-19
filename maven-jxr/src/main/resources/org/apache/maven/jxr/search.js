/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * The search box of the JXR pages without frames.
 *
 * Every page loads this script and has an empty element with the id "jxr_search" in its navigation bar, which is
 * where the box goes. It finds the packages and types that DirectoryIndexer lists in search-index.js. That file
 * sits next to this script, in the root directory of the report, and is not loaded before the box is used.
 */
(function () {
    "use strict";

    // how many packages and how many types are shown at most
    var MAX_RESULTS = 20;

    // the links of the index are relative to the root directory of the report, which is where this script is
    var scripts = document.getElementsByTagName("script");
    var script = document.currentScript || scripts[scripts.length - 1];
    var root = script.src.substring(0, script.src.lastIndexOf("/") + 1);

    var input;
    var list;
    var links = [];
    var selected = -1;
    var indexRequested = false;
    var indexFailed = false;

    function isUpperCase(c) {
        return c !== c.toLowerCase();
    }

    // "JCT" and "JaCoTr" both find JavaCodeTransform
    function matchesCamelCase(name, query) {
        var position = 0;
        for (var i = 0; i < query.length; i++) {
            var c = query.charAt(i);
            if (i > 0 && isUpperCase(c)) {
                while (position < name.length && !isUpperCase(name.charAt(position))) {
                    position++;
                }
            }
            if (name.charAt(position) !== c) {
                return false;
            }
            position++;
        }
        return true;
    }

    // 0 if the name is the query, 1 if it starts with it, 2 for camel case, 3 if it contains it, -1 if nothing fits
    function rank(name, query) {
        var lowerName = name.toLowerCase();
        var lowerQuery = query.toLowerCase();
        if (lowerName === lowerQuery) {
            return 0;
        }
        if (lowerName.indexOf(lowerQuery) === 0) {
            return 1;
        }
        if (query !== lowerQuery && matchesCamelCase(name, query)) {
            return 2;
        }
        return lowerName.indexOf(lowerQuery) > 0 ? 3 : -1;
    }

    // the best rank of the names an entry goes by
    function rankEntry(entry, query) {
        // a nested type and a package are also known by the last part of their name
        var names = [entry.l.substring(entry.l.lastIndexOf(".") + 1), entry.l];
        if (entry.p && query.indexOf(".") >= 0) {
            names.push(entry.p + "." + entry.l);
        }
        var best = -1;
        for (var i = 0; i < names.length; i++) {
            var current = rank(names[i], query);
            if (current >= 0 && (best < 0 || current < best)) {
                best = current;
            }
        }
        return best;
    }

    function compare(a, b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        return a < b ? -1 : (a > b ? 1 : 0);
    }

    // the entries that fit the query, the best first
    function find(entries, query) {
        var found = [];
        for (var i = 0; i < entries.length; i++) {
            var current = rankEntry(entries[i], query);
            if (current >= 0) {
                found.push({entry: entries[i], rank: current});
            }
        }
        found.sort(function (a, b) {
            return a.rank - b.rank
                || compare(a.entry.l, b.entry.l)
                || compare(a.entry.p || "", b.entry.p || "");
        });
        return found;
    }

    function addItem(className, text) {
        var item = document.createElement("li");
        item.className = className;
        if (text) {
            item.appendChild(document.createTextNode(text));
        }
        list.appendChild(item);
        return item;
    }

    function addResults(title, entries, query) {
        var found = find(entries, query);
        if (found.length === 0) {
            return;
        }
        addItem("jxr_search_category", title);
        for (var i = 0; i < found.length && i < MAX_RESULTS; i++) {
            var entry = found[i].entry;
            var link = document.createElement("a");
            link.href = root + entry.u;
            link.onclick = clearLater;
            link.appendChild(document.createTextNode(entry.l));
            if (entry.p) {
                var packageName = document.createElement("span");
                packageName.appendChild(document.createTextNode(" - " + entry.p));
                link.appendChild(packageName);
            }
            // a long name is cut off in the list
            link.title = entry.l + (entry.p ? " - " + entry.p : "");
            addItem("", "").appendChild(link);
            links.push(link);
        }
        if (found.length > MAX_RESULTS) {
            addItem("jxr_search_note", "... and " + (found.length - MAX_RESULTS) + " more");
        }
    }

    function clear() {
        while (list.firstChild) {
            list.removeChild(list.firstChild);
        }
        links = [];
        selected = -1;
        list.style.display = "none";
    }

    // a result can be on the page that is shown, which is not loaded anew then and would keep the list
    function clearLater() {
        window.setTimeout(clear, 0);
    }

    // below the box, in line with its right edge
    function show() {
        var box = input.getBoundingClientRect();
        var right = document.documentElement.clientWidth - box.right - window.pageXOffset;
        list.style.top = (box.bottom + window.pageYOffset) + "px";
        list.style.right = Math.max(right, 0) + "px";
        list.style.display = "block";
    }

    function loadIndex() {
        if (indexRequested) {
            return;
        }
        indexRequested = true;

        // a script element works for pages that are opened from the file system as well
        var index = document.createElement("script");
        index.type = "text/javascript";
        index.src = root + "search-index.js";
        // a file that is not the index, like the error page of a server, counts as loaded too
        index.onload = index.onerror = function () {
            indexFailed = !window.jxrSearchIndex;
            if (document.activeElement === input) {
                search();
            }
        };
        document.getElementsByTagName("head")[0].appendChild(index);
    }

    function search() {
        var query = input.value.replace(/^\s+|\s+$/g, "");
        var index = window.jxrSearchIndex;

        clear();
        if (!index) {
            // the search goes on when the index is there
            loadIndex();
        }
        if (query === "") {
            return;
        }
        if (index) {
            addResults("Classes", index.types, query);
            addResults("Packages", index.packages, query);
            if (links.length === 0) {
                addItem("jxr_search_note", "No results found");
            }
        } else {
            addItem("jxr_search_note",
                indexFailed ? "The search index could not be loaded" : "Loading the search index ...");
        }
        show();
    }

    function select(index) {
        if (links.length === 0) {
            return;
        }
        if (selected >= 0) {
            links[selected].parentNode.className = "";
        }
        selected = index;

        // keep the selected result in sight when the list has a scroll bar
        var item = links[selected].parentNode;
        item.className = "jxr_search_selected";
        if (item.offsetTop < list.scrollTop) {
            list.scrollTop = item.offsetTop;
        } else if (item.offsetTop + item.offsetHeight > list.scrollTop + list.clientHeight) {
            list.scrollTop = item.offsetTop + item.offsetHeight - list.clientHeight;
        }
    }

    function onKeyDown(event) {
        // keys that go to an input method, like the Enter that ends a composition
        if (event.isComposing || event.keyCode === 229) {
            return;
        }

        var key = event.key;
        var down = key === "ArrowDown" || key === "Down";
        if (down || key === "ArrowUp" || key === "Up") {
            if (links.length === 0) {
                // the list was closed with Escape
                search();
            }
            if (down) {
                select(selected + 1 < links.length ? selected + 1 : 0);
            } else {
                select(selected > 0 ? selected - 1 : links.length - 1);
            }
        } else if (key === "Enter") {
            if (links.length > 0) {
                var href = links[Math.max(selected, 0)].href;
                clear();
                window.location.href = href;
            }
        } else if (key === "Escape" || key === "Esc") {
            clear();
        } else {
            return;
        }
        event.preventDefault();
    }

    function init() {
        var box = document.getElementById("jxr_search");
        if (!box) {
            return;
        }

        var label = document.createElement("label");
        label.htmlFor = "jxr_search_input";
        label.appendChild(document.createTextNode("Search:"));
        box.appendChild(label);

        input = document.createElement("input");
        input.type = "text";
        input.id = "jxr_search_input";
        input.setAttribute("autocomplete", "off");
        box.appendChild(input);

        // the navigation bar cuts off what does not fit in, so the results are not a part of it but are placed
        // below the box
        list = document.createElement("ul");
        list.id = "jxr_search_results";
        list.style.position = "absolute";
        list.style.zIndex = "1";
        list.style.display = "none";
        document.body.appendChild(list);

        // a plain look for the results in case the style sheet has no rules for them, as a custom one may. It
        // comes before any style sheet, so that the rules of a style sheet take precedence.
        var style = document.createElement("style");
        style.type = "text/css";
        style.appendChild(document.createTextNode(
            "#jxr_search_results { margin: 0; padding: 0; border: 1px solid #888; background-color: #fff;"
            + " color: #000; list-style: none; }"
            + " #jxr_search_results a { display: block; }"
            + " #jxr_search_results li.jxr_search_selected a { background-color: #ddd; }"));
        var head = document.getElementsByTagName("head")[0];
        head.insertBefore(style, head.firstChild);

        input.addEventListener("focus", search);
        input.addEventListener("input", search);
        input.addEventListener("keydown", onKeyDown);
        input.addEventListener("blur", clear);
        window.addEventListener("resize", function () {
            if (list.style.display !== "none") {
                show();
            }
        });

        // a click on a result must not take the focus from the box, the list would be gone before the link is
        // followed
        list.addEventListener("mousedown", function (event) {
            event.preventDefault();
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
