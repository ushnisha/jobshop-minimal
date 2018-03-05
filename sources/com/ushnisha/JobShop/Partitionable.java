/**
 **********************************************************************
 * JobShop Minimal - A minimal JobShop Scheduler
 **********************************************************************
   Copyright (c) 2017-2018 Arun Kunchithapatham

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   Contributors:
   Arun Kunchithapatham - Initial Contribution
 ***********************************************************************
 *
 */
 
package com.ushnisha.JobShop;

/** An inteface that represents a group of partitionalble classes
 *  within a jobshop model
 */
public interface Partitionable {

    /**
     * returns the partitionid of the Partitionalble object
     * @return int value that represents the partitionid of the 
     *         Partitionable object
     */
    public abstract int getPartitionId();

    /**
     * sets the partitionid field of the Partitionalble object
     * @param pid integer representing the partitionid of the
     *        Partitionalble object
     */
    public abstract void setPartitionId(int pid);

    /**
     * propagates the partitionid field of the Partitionalble object
     * @param pid integer representing the partitionid of the object
     * @param check boolean value; if true, then propagate only if
     *        partitionid is not equal to pid
     */
    public abstract void propagatePartitionId(int pid, boolean check);

    /**
     * returns a string representation of the Partitionalble object
     * for logging during partitioning
     * @return String value that represents the Partitionable object
     */
    public abstract String partitionLogString();
}
