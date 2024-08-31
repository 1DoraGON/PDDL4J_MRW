# A* vs. Monte Carlo Tree Search with Pure Random Walk (MRW) Comparison

This project compares the performance of the A* search algorithm and Monte Carlo Tree Search with Pure Random Walk (MRW) using the PDDL4J framework.

## Project Structure

- **`planner_runner.py`**: This script compares the performance of A* and Pure Random Walk algorithms on the provided benchmarks. It calculates metrics like total runtime and makespan for each algorithm and generates a PDF file with the results.

- **Benchmarks**: The project uses a variety of PDDL benchmarks to evaluate the performance of the two algorithms. These benchmarks are `(blocksworld, depot, gripper and logistics)`.

- **Comparison PDF**: The results of the comparison, including runtime and makespan analysis, are documented in a PDF file included in the repository.

## Running the Code

To run this project, you need to place the contents of this repository at the root directory of the PDDL4J framework. This setup is necessary to ensure the correct paths and dependencies are recognized.

### Steps:

1. **Clone the Repository**: 
   ```bash
   git clone https://github.com/1DoraGON/PDDL4J_MRW.git
   ```
2. **Move the Files**: 
   Move all files from this repository to the root directory of your PDDL4J installation.

3. **Run the Planner Runner Script**:
   ```bash
   python planner_runner.py
   ```

This will execute the comparison between the A* and MRW algorithms using the specified benchmarks and generate the results.
