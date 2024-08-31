import os
import subprocess
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

# Define the commands and directories for MRW and ASP planners
commands = [
    {
        "directory": "MRW/",
        "label": "MCTS",
        "domain": "gripper",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.mrw.MRW",
            "../src/test/resources/benchmarks/pddl/ipc1998/gripper/adl/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc1998/gripper/adl/p01.pddl",
            "-n", "10000", "-lw", "20"
        ]
    },
    {
        "directory": "MRW/",
        "label": "MCTS",
        "domain": "logistics",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.mrw.MRW",
            "../src/test/resources/benchmarks/pddl/ipc2000/logistics/strips-typed/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc2000/logistics/strips-typed/p01.pddl",
            "-n", "10000", "-lw", "20"
        ]
    },
    {
        "directory": "MRW/",
        "label": "MCTS",
        "domain": "blocks",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.mrw.MRW",
            "../src/test/resources/benchmarks/pddl/ipc2000/blocks/strips-typed/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc2000/blocks/strips-typed/p001.pddl",
            "-n", "10000", "-lw", "20"
        ]
    },
    {
        "directory": "MRW/",
        "label": "MCTS",
        "domain": "depots",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.mrw.MRW",
            "../src/test/resources/benchmarks/pddl/ipc2002/depots/strips-automatic/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc2002/depots/strips-automatic/p01.pddl",
            "-n", "10000", "-lw", "20"
        ]
    },
    {
        "directory": "ASP/",
        "label": "HSP",
        "domain": "gripper",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.asp.ASP",
            "../src/test/resources/benchmarks/pddl/ipc1998/gripper/adl/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc1998/gripper/adl/p01.pddl",
            "-w", "1.2", "-t", "30000"
        ]
    },
    {
        "directory": "ASP/",
        "label": "HSP",
        "domain": "logistics",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.asp.ASP",
            "../src/test/resources/benchmarks/pddl/ipc2000/logistics/strips-typed/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc2000/logistics/strips-typed/p01.pddl",
            "-w", "1.2", "-t", "30000"
        ]
    },
    {
        "directory": "ASP/",
        "label": "HSP",
        "domain": "blocks",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.asp.ASP",
            "../src/test/resources/benchmarks/pddl/ipc2000/blocks/strips-typed/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc2000/blocks/strips-typed/p001.pddl",
            "-w", "1.2", "-t", "30000"
        ]
    },
    {
        "directory": "ASP/",
        "label": "HSP",
        "domain": "depots",
        "command": [
            "java", "-cp", "classes;lib/pddl4j-4.0.0.jar",
            "fr.uga.pddl4j.examples.asp.ASP",
            "../src/test/resources/benchmarks/pddl/ipc2002/depots/strips-automatic/domain.pddl",
            "../src/test/resources/benchmarks/pddl/ipc2002/depots/strips-automatic/p01.pddl",
            "-w", "1.2", "-t", "30000"
        ]
    },
    # Add more command entries for other domains/problems here
]

# Storage for results
results = []

# Run each command and collect the metrics
for cmd in commands:
    os.chdir(cmd["directory"])
    print(cmd['domain'])
    
    result = subprocess.run(cmd["command"], capture_output=True, text=True)

    # print(result)
    # Extract time and makespan from the output
    output = result.stdout

    # print(output)
    # Extract total time spent
    time_line = [line for line in output.splitlines() if "total time" in line][0]

    time_spent = float(time_line.split()[0].replace(',', '.'))
    # Extract plan and calculate the makespan (plan length)
    plan_start_idx = output.find("found plan as follows:") + len("found plan as follows:")
    plan_end_idx = output.find("time spent:")
    plan = output[plan_start_idx:plan_end_idx].strip().splitlines()
    makespan = len(plan)

    # print(makespan)

    # Store the result
    results.append({
        "planner": cmd["label"],
        "domain": cmd['domain'], 
        "problem": "p01",    
        "time": time_spent,
        "makespan": makespan
    })
    print(makespan,' ',time_spent)
    
    os.chdir("..")
print(results)
# Separate the results by domain and planner
domain_results = {}
for result in results:
    key = f"{result['domain']}_{result['problem']}"
    if key not in domain_results:
        domain_results[key] = {"MCTS": {}, "HSP": {}}
    domain_results[key][result["planner"]] = result

# Plotting and saving the results in a PDF
pdf_filename = "planner_comparisons.pdf"
with PdfPages(pdf_filename) as pdf:
    for key, data in domain_results.items():
        plt.figure(figsize=(10, 5))

        # Time comparison
        plt.subplot(1, 2, 1)
        plt.bar(["MCTS", "HSP"], [data["MCTS"]["time"], data["HSP"]["time"]], color=['blue', 'green'])
        plt.title(f"{key} - Time Comparison")
        plt.ylabel("Time (s)")

        # Makespan comparison
        plt.subplot(1, 2, 2)
        plt.bar(["MCTS", "HSP"], [data["MCTS"]["makespan"], data["HSP"]["makespan"]], color=['blue', 'green'])
        plt.title(f"{key} - Makespan Comparison")
        plt.ylabel("Makespan")

        # Save the figure in the PDF
        plt.suptitle(f"Comparison for {key}")
        pdf.savefig()
        plt.close()

print(f"All plots have been saved to {pdf_filename}")