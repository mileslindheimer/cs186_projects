DROP VIEW IF EXISTS q1a, q1b, q1c, q1d, q2, q3, q4, q5, q6, q7;

-- Question 1a
CREATE VIEW q1a(id, amount)
AS
  SELECT cmte_id, transaction_amt 
	FROM committee_contributions
	WHERE transaction_amt > 5000.00
	ORDER BY cmte_id
;

-- Question 1b
CREATE VIEW q1b(id, name, amount)
AS
  SELECT cmte_id, name, transaction_amt
	FROM committee_contributions
	WHERE transaction_amt > 5000.00
	ORDER BY cmte_id, name
;

-- Question 1c
CREATE VIEW q1c(id, name, avg_amount)
AS
  SELECT cmte_id, name, AVG(transaction_amt)
	FROM committee_contributions
	WHERE transaction_amt > 5000.00
	GROUP BY cmte_id, name
;

-- Question 1d
CREATE VIEW q1d(id, name, avg_amount)
AS
  SELECT cmte_id, name, AVG(transaction_amt)
	FROM committee_contributions
	WHERE transaction_amt > 5000.00 
	GROUP BY cmte_id, name
	HAVING AVG(transaction_amt) > 10000.00
;

-- Question 2
CREATE VIEW q2(from_name, to_name)
AS
  SELECT b.name, a.name
	FROM committees a, committees b, intercommittee_transactions t
	WHERE (a.id = t.cmte_id AND b.id = t.other_id AND a.pty_affiliation = 'DEM' and b.pty_affiliation = 'DEM')
	GROUP BY a.name, b.name
	ORDER BY COUNT(*) DESC
	LIMIT 10
;

-- Question 3
CREATE VIEW q3(name)
AS
	WITH obama AS (
  	SELECT id, name FROM candidates c WHERE name='OBAMA, BARACK'
  ),
	support AS (
		SELECT con.cand_id, con.cmte_id
		FROM committee_contributions con
		INNER JOIN obama o ON (con.cand_id=o.id or con.cmte_id=o.id)
		GROUP BY con.cand_id,con.cmte_id
	)
	SELECT comm.name 
	FROM committees comm
  WHERE comm.id NOT IN (SELECT cmte_id FROM support)
	GROUP BY comm.id 
;

-- Question 4.
CREATE VIEW q4 (name)
AS
	WITH contributions AS (SELECT DISTINCT a.id, b.cmte_id 
		FROM candidates a, committee_contributions b
		WHERE (b.cand_id IS NULL AND a.id=b.cmte_id) OR a.id=b.cand_id 
		GROUP BY a.id, b.cmte_id
	)
  SELECT cand.name
	FROM candidates cand, contributions con
	WHERE cand.id=con.id
	GROUP BY cand.id
	HAVING (CAST(COUNT(*) AS float) * 100.0) / CAST(((SELECT COUNT(id) FROM committees)) AS float) > 1.0
;

-- Question 5
CREATE VIEW q5 (name, total_pac_donations) AS
	SELECT a.name, SUM(b.transaction_amt)
	FROM committees a 
	LEFT OUTER JOIN individual_contributions b 
		ON b.entity_tp='ORG' AND a.id=b.cmte_id
	GROUP BY a.id 	
;

-- Question 6
CREATE VIEW q6 (id) AS
	SELECT DISTINCT a.cand_id
	FROM committee_contributions a, committee_contributions b
	WHERE a.cand_id=b.cand_id
	GROUP BY a.cand_id, a.entity_tp, b.entity_tp
	HAVING a.entity_tp='PAC' AND b.entity_tp='CCM'
;

-- Question 7
CREATE VIEW q7 (cand_name1, cand_name2) AS
	SELECT a.name, b.name
	FROM candidates a, candidates b, committee_contributions c, committee_contributions d 
	WHERE c.cmte_id=d.cmte_id 
		AND c.state='RI' AND d.state='RI' 
		AND (a.id=c.cand_id AND b.id=d.cand_id)
		AND (NOT a.id=d.cand_id AND NOT b.id=c.cand_id)
	GROUP BY a.id, b.id
	HAVING NOT a.id=b.id
;
